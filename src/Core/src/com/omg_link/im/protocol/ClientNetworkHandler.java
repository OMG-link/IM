package com.omg_link.im.protocol;

import com.omg_link.im.Client;
import com.omg_link.im.config.Config;
import com.omg_link.im.message_manager.ClientMessageManager;
import com.omg_link.im.protocol.data.ByteArrayInfo;
import com.omg_link.im.protocol.data.ByteData;
import com.omg_link.im.protocol.data.InvalidPackageException;
import com.omg_link.im.protocol.data.PackageTooLargeException;
import com.omg_link.im.protocol.data_pack.DataPack;
import com.omg_link.im.protocol.data_pack.file_transfer.*;
import com.omg_link.im.protocol.data_pack.system.*;
import com.omg_link.im.protocol.data_pack.user_list.*;
import com.omg_link.im.protocol.file_transfer.ClientFileReceiveTask;
import com.omg_link.im.protocol.file_transfer.ClientFileSendTask;
import com.omg_link.im.protocol.file_transfer.NoSuchTaskIdException;

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
    private final Client client;
    private final ClientMessageManager messageManager;
    private final String url;
    private final int port;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    private Timer pingTimer;

    private boolean interrupted = false;

    private DataPack.Type expectedSendType = DataPack.Type.CheckVersion;
    private DataPack.Type expectedReceiveType = DataPack.Type.Undefined;

    public boolean isInterrupted() {
        return interrupted;
    }

    public void interrupt() {
        if (interrupted) return;
        interrupted = true;
        this.close();
    }

    private void exit(String reason) {
        if (client.getRoomFrame() != null) {
            client.getRoomFrame().exitRoom(reason);
        } else {
            System.exit(0);
        }
    }

    public ClientNetworkHandler(Client client, String url, int port) {
        this.client = client;
        this.messageManager = new ClientMessageManager(client);
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
            this.exit("Unknown host!");
        } catch (Exception e) {
            e.printStackTrace();
            this.exit(e.toString());
        }
    }

    public void close() {
        interrupted = true;
        //Notice the roomFrame
        if (this.client.getRoomFrame() != null) {
            this.client.getRoomFrame().onConnectionBroke();
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
        } catch (IOException e) {
            //do nothing
        } finally {
            this.outputStream = null;
            this.inputStream = null;
        }
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
            //If IOException is caused by interrupt, just return.
            if (this.isInterrupted()) return;
            this.close();
            client.runNetworkHandler();
        } catch (InvalidPackageException e) {
            e.printStackTrace();
            this.close();
            this.exit("Client received an invalid package. Connection has been closed.");
        } catch (Exception e) {
            this.client.showException(e);
            this.close();
            this.exit(e.toString());
        }
    }

    public void send(DataPack dataPack) throws PackageTooLargeException {
        if (isInterrupted()) return;

        if (expectedSendType != null) {
            if (dataPack.getType() != expectedSendType) {
                client.getLogger().log(
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
            this.client.showInfo("Connection Error!");
            this.close();
        }
    }

    private void onReceive(ByteData data) throws InvalidPackageException {
        DataPack.Type type = data.peekEnum(DataPack.Type.values());

        if (expectedReceiveType != null && type != expectedReceiveType) {
            client.getLogger().log(
                    Level.WARNING,
                    String.format("The client is expecting package of type %s, but we received %s. The package will not be processed.", expectedSendType, type)
            );
            return;
        }

        switch (type) {
            case CheckVersion: {
                IVersionGetter versionGetter;
                //I know it loops only once. I just need to break somewhere inside it.
                while (true) {
                    try {
                        versionGetter = new CheckVersionPackV2(data);
                        break;
                    } catch (InvalidPackageException ignored) {}

                    try {
                        versionGetter = new CheckVersionPackV1(data);
                        break;
                    } catch (InvalidPackageException ignored) {}

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
                    this.close();
                    if (versionGetter.getVersion() == null) {
                        this.client.getGUI().alertVersionUnrecognizable(Config.version);
                    } else {
                        this.client.getGUI().alertVersionIncompatible(versionGetter.getVersion(), Config.version);
                    }
                } else {
                    if (!Objects.equals(versionGetter.getVersion(), Config.version)) {
                        this.client.getGUI().alertVersionMismatch(versionGetter.getVersion(), Config.version);
                    }
                    expectedSendType = DataPack.Type.ConnectRequest;
                    expectedReceiveType = DataPack.Type.Undefined;

                    send(new ConnectRequestPack(
                            client.getUserManager().getCurrentUser().getName(),
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
                messageManager.receive(data);
                break;
            }
            case ConnectResult: {
                var pack = new ConnectResultPack(data);
                if (pack.isTokenAccepted()) {
                    client.getUserManager().getCurrentUser().setUid(pack.getUid());
                    onConnectionBuilt();
                } else {
                    this.close();
                    client.getRoomFrame().onConnectionRefused(pack.getRejectReason());
                }
                break;
            }
            case SetRoomName: {
                var pack = new SetRoomNamePack(data);
                this.client.getRoomFrame().onRoomNameUpdate(pack.getRoomName());
                break;
            }
            case BroadcastUserList: {
                var pack = new BroadcastUserListPack(data);
                this.client.getUserManager().updateFromUserList(pack.getUserList());
                break;
            }
            case BroadcastUserJoin: {
                var pack = new BroadcastUserJoinPack(data);
                this.client.getUserManager().joinUser(pack.getUser());
                break;
            }
            case BroadcastUserLeft: {
                var pack = new BroadcastUserLeftPack(data);
                this.client.getUserManager().removeUser(pack.getUid());
                break;
            }
            case BroadcastUserNameChanged: {
                var pack = new BroadcastUsernameChangedPack(data);
                this.client.getUserManager().changeUsername(pack.getUid(), pack.getName());
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
                        ClientFileReceiveTask task = client.getFactoryManager().getFileReceiveTaskFactory().find(pack.getReceiverTaskId());
                        try {
                            task.setFileSize(pack.getFileSize());
                            ok = true;
                            reason = UploadReplyPack.Reason.ok;
                        } catch (IOException e) {
                            ok = false;
                            reason = UploadReplyPack.Reason.remoteIOError;
                        }
                    } catch (NoSuchTaskIdException e) {
                        client.getLogger().log(
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
                    ClientFileSendTask task = client.getFactoryManager().getFileSendTaskFactory().find(pack.getSenderTaskId());
                    task.onReceiveUploadReply(pack);
                } catch (NoSuchTaskIdException e) {
                    client.getLogger().log(
                            Level.WARNING,
                            String.format("Client received a FileUploadReplyPack for {senderTaskId=%s}, but it is not found on client.", pack.getSenderTaskId())
                    );
                }
                break;
            }
            case FileUploadFinish: {
                UploadFinishPack pack = new UploadFinishPack(data);
                try {
                    ClientFileReceiveTask task = client.getFactoryManager().getFileReceiveTaskFactory().find(pack.getReceiverTaskId());
                    task.end();
                } catch (NoSuchTaskIdException e) {
                    client.getLogger().log(
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
                    ClientFileSendTask task = client.getFactoryManager().getFileSendTaskFactory().find(pack.getSenderTaskId());
                    if (pack.isOk()) {
                        task.onEndSucceed();
                    } else {
                        task.onEndFailed(pack.getReason());
                    }
                } catch (NoSuchTaskIdException e) {
                    client.getLogger().log(
                            Level.WARNING,
                            String.format("Client received a FileUploadResult for {senderTaskId=%s}, but it is not found in client.", pack.getSenderTaskId())
                    );
                }
                break;
            }
            case FileDownloadReply: {
                DownloadReplyPack pack = new DownloadReplyPack(data);
                try {
                    ClientFileReceiveTask task = client.getFactoryManager().getFileReceiveTaskFactory().find(pack.getReceiverTaskId());
                    if (pack.isOk()) {
                        task.setSenderTaskId(pack.getSenderTaskId());
                    } else {
                        switch (pack.getReason()){
                            case ok:{
                                task.onEndFailed("??????????");
                                break;
                            }
                            case fileNotFound:{
                                task.onEndFailed("File not found on server.");
                                break;
                            }
                        }
                    }
                } catch (NoSuchTaskIdException e) {
                    client.getLogger().log(
                            Level.WARNING,
                            String.format("Client received a FileDownloadReplyPack for {receiverTaskId=%s}, but it is not found in client.", pack.getReceiverTaskId())
                    );
                }
                break;
            }
            case FileContent: {
                FileContentPack pack = new FileContentPack(data);
                try {
                    ClientFileReceiveTask task = client.getFactoryManager().getFileReceiveTaskFactory().find(pack.getReceiverTaskId());
                    try {
                        task.onDataReceived(pack.getData());
                    } catch (IOException e) {
                        task.end();
                    }
                } catch (NoSuchTaskIdException e) {
                    client.getLogger().log(
                            Level.WARNING,
                            String.format("Client received a FileContent for {receiverTaskId=%s}, but it is not found in client.", pack.getReceiverTaskId())
                    );
                }
                break;
            }
            default: {
                client.getLogger().log(
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
                try {
                    client.getNetworkHandler().send(new PingPack());
                } catch (PackageTooLargeException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 5000, 10 * 1000);

        client.onConnectionBuilt();
    }

    public ClientMessageManager getMessageManager() {
        return messageManager;
    }

}
