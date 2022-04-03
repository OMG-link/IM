package com.omg_link.im.core.protocol;

import com.omg_link.im.core.ClientRoom;
import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.gui.IRoomFrame;
import com.omg_link.im.core.protocol.data.ByteArrayInfo;
import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data.PackageTooLargeException;
import com.omg_link.im.core.protocol.data_pack.DataPack;
import com.omg_link.im.core.protocol.data_pack.file_transfer.*;
import com.omg_link.im.core.protocol.data_pack.system.*;
import com.omg_link.im.core.protocol.data_pack.user_list.*;
import com.omg_link.im.core.protocol.file_transfer.ClientFileReceiveTask;
import com.omg_link.im.core.protocol.file_transfer.ClientFileSendTask;
import com.omg_link.im.core.protocol.file_transfer.NoSuchTaskIdException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

public class ClientNetworkHandler implements Runnable {
    private final ClientRoom room;
    private final String url;
    private final int port;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    private Timer pingTimer;

    private boolean stopped = false;

    private DataPack.Type expectedSendType = DataPack.Type.CheckVersion;
    private DataPack.Type expectedReceiveType = DataPack.Type.Undefined;

    public ClientNetworkHandler(ClientRoom room, String url, int port) {
        this.room = room;
        this.url = url;
        this.port = port;
    }

    public void connect() {
        try {
            this.socket = new Socket(url, port);
            this.outputStream = this.socket.getOutputStream();
            this.inputStream = this.socket.getInputStream();

            Thread thread = new Thread(this, "Client Receive Thread");
            thread.start();

        } catch (UnknownHostException e) {
            room.exitRoom(IRoomFrame.ExitReason.InvalidUrl);
        } catch (Exception e) {
            e.printStackTrace();
            room.exitRoom(IRoomFrame.ExitReason.ClientException);
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    /**
     * <p>Stop network connection.</p>
     * <p>You possibly need to call this through {@code room.exitRoom()}.</p>
     */
    public void stop() {
        if (stopped) return;
        stopped = true;
        //Notice the roomFrame
        if (room.getRoomFrame() != null) {
            room.getRoomFrame().onConnectionBroke();
        }
        //Cancel pingTimer
        if (pingTimer != null) {
            pingTimer.cancel();
        }
        //Close socket
        try {
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (IOException ignored) {
        }
        this.outputStream = null;
        this.inputStream = null;
    }

    @Override
    public void run() {
        try {
            while (true) {
                ByteData lengthBuffer = new ByteData(this.inputStream, 4);
                int length = lengthBuffer.decodeInt();
                if (length > Config.packageMaxLength) {
                    throw new InvalidPackageException();
                }
                ByteData data = new ByteData(this.inputStream, length);
                this.onReceive(data);
            }
        } catch (IOException e) {
            if (isStopped()) {
                return; //If IOException is caused by stop(), just return.
            }
            room.reconnect();
        } catch (InvalidPackageException e) {
            e.printStackTrace();
            room.exitRoom(IRoomFrame.ExitReason.PackageDecodeError);
        } catch (Exception e) {
            e.printStackTrace();
            room.exitRoom(IRoomFrame.ExitReason.ClientException);
        }
    }

    public void send(DataPack dataPack) throws PackageTooLargeException {
        if (isStopped()) return;

        if (expectedSendType != null) {
            if (dataPack.getType() != expectedSendType) {
                room.getLogger().log(
                        Level.WARNING,
                        String.format("The client wants to send package of type %s, but it is expected to send %s. This package will not be sent.", dataPack.getType(), expectedSendType)
                );
                return;
            } else {
                switch (dataPack.getType()) {
                    case CheckVersion: {
                        expectedSendType = DataPack.Type.Undefined;
                        expectedReceiveType = DataPack.Type.CheckVersion;
                        break;
                    }
                    case ConnectRequest: {
                        expectedSendType = DataPack.Type.Undefined;
                        expectedReceiveType = DataPack.Type.ConnectResult;
                        break;
                    }
                }
            }
        }

        this.send(dataPack.encode());

    }

    private synchronized void send(ByteData data) throws PackageTooLargeException {
        if (data.getLength() > Config.packageMaxLength) {
            throw new PackageTooLargeException();
        }

        ByteData rawData = new ByteData();
        rawData.append(ByteData.encode(data.getLength()));
        rawData.append(data);

        try {
            ByteArrayInfo arrayInfo = rawData.getData();
            outputStream.write(arrayInfo.getArray(), arrayInfo.getOffset(), arrayInfo.getLength());
            outputStream.flush();
        } catch (IOException e) {
            room.reconnect();
        }

    }

    private void onReceive(ByteData data) throws InvalidPackageException {
        DataPack.Type type = data.peekEnum(DataPack.Type.values());

        if (expectedReceiveType != null && type != expectedReceiveType) {
            room.getLogger().log(
                    Level.WARNING,
                    String.format("The client is expecting package of type %s, but we received %s. The package will not be processed.", expectedSendType, type)
            );
            return;
        }

        switch (type) {
            case CheckVersion: {
                IVersionGetter versionGetter;
                // I know it loops only once. I just need to break somewhere inside it.
                while (true) {
                    try {
                        versionGetter = new CheckVersionPackV2(data);
                        break;
                    } catch (InvalidPackageException ignored) {
                    }

                    try {
                        versionGetter = new CheckVersionPackV1(data);
                        break;
                    } catch (InvalidPackageException ignored) {
                    }

                    versionGetter = new IVersionGetter() {
                        @Override
                        public String getVersion() {
                            return null;
                        }

                        @Override
                        public String getCompatibleVersion() {
                            return null;
                        }
                    };
                    break;
                }

                if (!Objects.equals(versionGetter.getCompatibleVersion(), Config.compatibleVersion)) {
                    this.stop(); // Didn't call room.exitRoom() because we need to exit after the dialog closed.
                    if (versionGetter.getVersion() == null) {
                        room.alertVersionUnrecognizable();
                    } else {
                        room.alertVersionIncompatible(versionGetter.getVersion());
                    }
                } else {
                    if (!Objects.equals(versionGetter.getVersion(), Config.version)) {
                        room.alertVersionMismatch(versionGetter.getVersion());
                    }
                    expectedSendType = DataPack.Type.ConnectRequest;
                    expectedReceiveType = DataPack.Type.Undefined;

                    send(new ConnectRequestPack(
                            room.getUserManager().getCurrentUser().getName(),
                            Config.getToken()
                    ));
                }

                break;
            }
            case ChatHistory:
            case ChatSendReply:
            case ChatText:
            case ChatImage:
            case ChatFile: {
                room.getMessageManager().receive(data);
                break;
            }
            case ConnectResult: {
                var pack = new ConnectResultPack(data);
                if (pack.isTokenAccepted()) {
                    room.getUserManager().getCurrentUser().setUid(pack.getUid());
                    onConnectionBuilt();
                } else {
                    this.stop();
                    IRoomFrame.ExitReason reason;
                    switch (pack.getRejectReason()){
                        case InvalidToken:{
                            reason = IRoomFrame.ExitReason.InvalidToken;
                            break;
                        }
                        default:{
                            reason = IRoomFrame.ExitReason.Unknown;
                            break;
                        }
                    }
                    room.exitRoom(reason);
                }
                break;
            }
            case SetRoomName: {
                var pack = new SetRoomNamePack(data);
                room.getRoomFrame().onRoomNameUpdate(pack.getRoomName());
                break;
            }
            case BroadcastUserList: {
                var pack = new BroadcastUserListPack(data);
                room.getUserManager().updateFromUserList(pack.getUserList());
                break;
            }
            case BroadcastUserJoin: {
                var pack = new BroadcastUserJoinPack(data);
                room.getUserManager().joinUser(pack.getUser());
                break;
            }
            case BroadcastUserLeft: {
                var pack = new BroadcastUserLeftPack(data);
                room.getUserManager().removeUser(pack.getUid());
                break;
            }
            case BroadcastUserNameChanged: {
                var pack = new BroadcastUsernameChangedPack(data);
                room.getUserManager().changeUsername(pack.getUid(), pack.getName());
                break;
            }
            case FileUploadRequest: {
                var pack = new UploadRequestPack(data);
                boolean ok;
                UploadReplyPack.Reason reason;
                if (pack.getFileSize() > Config.fileMaxSize) {
                    ok = false;
                    reason = UploadReplyPack.Reason.fileTooLarge;
                } else {
                    try {
                        ClientFileReceiveTask task = room.getFactoryManager().getFileReceiveTaskFactory().find(pack.getReceiverTaskId());
                        try {
                            task.setFileSize(pack.getFileSize());
                            ok = true;
                            reason = UploadReplyPack.Reason.ok;
                        } catch (IOException e) {
                            ok = false;
                            reason = UploadReplyPack.Reason.remoteIOError;
                        }
                    } catch (NoSuchTaskIdException e) {
                        room.getLogger().log(
                                Level.WARNING,
                                String.format("Client received a FileUploadRequest for {receiverTaskId=%s}, but it is not found in client.", pack.getReceiverTaskId())
                        );
                        ok = false;
                        reason = UploadReplyPack.Reason.taskNotFound;
                    }
                }
                try {
                    this.send(new UploadReplyPack(
                            pack,
                            ok,
                            reason
                    ));
                } catch (PackageTooLargeException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case FileUploadReply: {
                UploadReplyPack pack = new UploadReplyPack(data);
                try {
                    ClientFileSendTask task = room.getFactoryManager().getFileSendTaskFactory().find(pack.getSenderTaskId());
                    task.onReceiveUploadReply(pack);
                } catch (NoSuchTaskIdException e) {
                    room.getLogger().log(
                            Level.WARNING,
                            String.format("Client received a FileUploadReplyPack for {senderTaskId=%s}, but it is not found on client.", pack.getSenderTaskId())
                    );
                }
                break;
            }
            case FileUploadFinish: {
                UploadFinishPack pack = new UploadFinishPack(data);
                try {
                    ClientFileReceiveTask task = room.getFactoryManager().getFileReceiveTaskFactory().find(pack.getReceiverTaskId());
                    task.end();
                } catch (NoSuchTaskIdException e) {
                    room.getLogger().log(
                            Level.WARNING,
                            String.format("Client received a FileUploadFinish for {receiverTaskId=%s}, but it is not found in client.", pack.getReceiverTaskId())
                    );
                    try {
                        send(new UploadResultPack(pack.getSenderTaskId()));
                    } catch (PackageTooLargeException e2) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            }
            case FileUploadResult: {
                UploadResultPack pack = new UploadResultPack(data);
                try {
                    ClientFileSendTask task = room.getFactoryManager().getFileSendTaskFactory().find(pack.getSenderTaskId());
                    if (pack.isOk()) {
                        task.onEndSucceed();
                    } else {
                        task.onEndFailed(pack.getReason());
                    }
                } catch (NoSuchTaskIdException e) {
                    room.getLogger().log(
                            Level.WARNING,
                            String.format("Client received a FileUploadResult for {senderTaskId=%s}, but it is not found in client.", pack.getSenderTaskId())
                    );
                }
                break;
            }
            case FileDownloadReply: {
                DownloadReplyPack pack = new DownloadReplyPack(data);
                try {
                    ClientFileReceiveTask task = room.getFactoryManager().getFileReceiveTaskFactory().find(pack.getReceiverTaskId());
                    if (pack.isOk()) {
                        task.setSenderTaskId(pack.getSenderTaskId());
                    } else {
                        switch (pack.getReason()) {
                            case ok: {
                                task.onEndFailed("??????????");
                                break;
                            }
                            case fileNotFound: {
                                task.onEndFailed("File not found on server.");
                                break;
                            }
                        }
                    }
                } catch (NoSuchTaskIdException e) {
                    room.getLogger().log(
                            Level.WARNING,
                            String.format("Client received a FileDownloadReplyPack for {receiverTaskId=%s}, but it is not found in client.", pack.getReceiverTaskId())
                    );
                }
                break;
            }
            case FileContent: {
                FileContentPack pack = new FileContentPack(data);
                try {
                    ClientFileReceiveTask task = room.getFactoryManager().getFileReceiveTaskFactory().find(pack.getReceiverTaskId());
                    try {
                        task.onDataReceived(pack.getData());
                    } catch (IOException e) {
                        task.end();
                    }
                } catch (NoSuchTaskIdException e) {
                    room.getLogger().log(
                            Level.WARNING,
                            String.format("Client received a FileContent for {receiverTaskId=%s}, but it is not found in client.", pack.getReceiverTaskId())
                    );
                }
                break;
            }
            default: {
                room.getLogger().log(
                        Level.WARNING,
                        "Client was unable to handle package type: " + type
                );
                throw new InvalidPackageException();
            }
        }
    }

    public void onConnectionBuilt() {
        expectedSendType = null;
        expectedReceiveType = null;

        pingTimer = new Timer();
        pingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(isStopped()){
                    pingTimer.cancel();
                }else{
                    send(new PingPack());
                }
            }
        }, 5000, 10 * 1000);
        room.onConnectionBuilt();
    }

}
