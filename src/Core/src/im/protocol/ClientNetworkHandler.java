package im.protocol;

import im.Client;
import im.config.Config;
import im.protocol.data.ByteArrayInfo;
import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data.PackageTooLargeException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;
import im.protocol.data_pack.chat.ChatImagePack;
import im.protocol.data_pack.chat.FileUploadedPack;
import im.protocol.data_pack.chat.TextPack;
import im.protocol.data_pack.file_transfer.*;
import im.protocol.data_pack.system.CheckVersionPack;
import im.protocol.data_pack.system.CheckVersionPackV1;
import im.protocol.data_pack.system.PingPack;
import im.protocol.data_pack.user_list.*;
import im.protocol.fileTransfer.ClientFileReceiveTask;
import im.protocol.fileTransfer.ClientFileSendTask;
import im.protocol.fileTransfer.NoSuchTaskIdException;

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
    private final String url;
    private final int port;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    private Timer pingTimer;

    private boolean interrupted = false;

    private DataPackType expectedSendType = DataPackType.CheckVersion;
    private DataPackType expectedReceiveType = DataPackType.Undefined;

    public boolean isInterrupted(){
        return interrupted;
    }

    public void interrupt() {
        if (interrupted) return;
        interrupted = true;
        this.close();
    }

    public ClientNetworkHandler(Client client, String url, int port) {
        this.client = client;
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
            this.client.showInfo("Unknown host!");
            System.exit(0);
        } catch (Exception e) {
            this.client.showInfo(e.toString());
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void close() {
        interrupted = true;
        try {
            pingTimer.cancel();
            this.socket.close();
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
                int length = ByteData.decodeInt(lengthBuffer);
                if (length > Config.packageMaxLength) {
                    throw new InvalidPackageException();
                }
                ByteData data = new ByteData(this.inputStream, length);
                this.onReceive(data);
            }
        } catch (IOException e) {
            //If IOException is caused by interrupt, just return.
            if (this.isInterrupted()) return;
            //Otherwise, try auto reconnect.
            if (this.client.getRoomFrame() != null) {
                this.client.getRoomFrame().onMessageReceive(
                        "System",
                        System.currentTimeMillis(),
                        "Disconnected from the server, trying to reconnect."
                );
            }
            this.close();
            client.runNetworkHandler();
        } catch (InvalidPackageException e) {
            e.printStackTrace();
            this.client.showInfo("Client received an invalid package. Connection has been closed.");
            this.close();
            System.exit(0);
        } catch (Exception e) {
            this.client.showException(e);
            this.close();
            System.exit(0);
        }
    }

    public void send(DataPack dataPack) {
        if(isInterrupted()) return;

        if(expectedSendType!=null){
            if(dataPack.getType()!=expectedSendType){
                client.getLogger().log(
                        Level.WARNING,
                        String.format("The client wants to send package of type %s, but it is expected to send %s. This package will not be sent.",dataPack.getType(),expectedSendType)
                );
                return;
            }else{
                switch (dataPack.getType()){
                    case CheckVersion:{
                        expectedSendType = DataPackType.Undefined;
                        expectedReceiveType = DataPackType.CheckVersion;
                        break;
                    }
                    case SetUserName:{
                        expectedSendType = DataPackType.Undefined;
                        expectedReceiveType = DataPackType.SetUid;
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
            outputStream.write(arrayInfo.getArray(),arrayInfo.getOffset(),arrayInfo.getLength());
            outputStream.flush();
        } catch (IOException e) {
            this.client.showInfo("Connection Error!");
            this.close();
        }
    }

    private void onReceive(ByteData data) throws InvalidPackageException {
        DataPackType type = DataPackType.toType(ByteData.peekInt(data));

        if(expectedReceiveType!=null&&type!=expectedReceiveType){
            client.getLogger().log(
                    Level.WARNING,
                    String.format("The client is expecting package of type %s, but we received %s. The package will not be processed.",expectedSendType,type)
            );
            return;
        }

        switch (type) {
            case CheckVersion: {
                try{
                    CheckVersionPack pack = new CheckVersionPack(data);
                    if(!Objects.equals(pack.getCompatibleVersion(),Config.compatibleVersion)){
                        this.close();
                        this.client.getGUI().alertVersionIncompatible(pack.getVersion(),Config.version);
                    }else{
                        if (!Objects.equals(pack.getVersion(), Config.version)) {
                            this.client.getGUI().alertVersionMismatch(pack.getVersion(), Config.version);
                        }
                        expectedSendType = DataPackType.SetUserName;
                        expectedReceiveType = DataPackType.Undefined;

                        send(new SetUsernamePack(client.getUserManager().getCurrentUser().getName()));
                    }
                    break;
                }catch (InvalidPackageException ignored){
                }

                try{
                    CheckVersionPackV1 pack = new CheckVersionPackV1(data);
                    this.close();
                    this.client.getGUI().alertVersionIncompatible(pack.getVersion(),Config.version);
                    break;
                }catch (InvalidPackageException ignored){
                }

                this.close();
                this.client.getGUI().alertVersionUnrecognizable(Config.version);

                break;
            }
            case Text: {
                TextPack pack = new TextPack(data);
                this.client.getRoomFrame().onMessageReceive(
                        pack.getSender(),
                        pack.getStamp(),
                        pack.getText()
                );
                break;
            }
            case ChatImage: {
                ChatImagePack pack = new ChatImagePack(data);
                var callback = client.getRoomFrame().onChatImageReceive(pack.getSender(), pack.getStamp(), pack.getServerImageId());
                client.downloadFile(pack.getServerImageId().toString(), pack.getServerImageId(), FileTransferType.ChatImage, null, callback);
                break;
            }
            case FileUploaded: {
                FileUploadedPack pack = new FileUploadedPack(data);
                this.client.getRoomFrame().onFileUploadedReceive(
                        pack.getSender(),
                        pack.getStamp(),
                        pack.getFileId(),
                        pack.getFileName(),
                        pack.getFileSize()
                );
                break;
            }
            case SetUid:{
                SetUidPack pack = new SetUidPack(data);
                client.getUserManager().getCurrentUser().setUid(pack.getUid());
                onConnectionBuilt();
                break;
            }
            case SetRoomName:{
                SetRoomNamePack pack = new SetRoomNamePack(data);
                this.client.getRoomFrame().onRoomNameUpdate(pack.getRoomName());
                break;
            }
            case BroadcastUserList: {
                BroadcastUserListPack pack = new BroadcastUserListPack(data);
                this.client.getUserManager().updateFromUserList(pack.getUserList());
                break;
            }
            case BroadcastUserJoin:{
                BroadcastUserJoinPack pack = new BroadcastUserJoinPack(data);
                this.client.getUserManager().joinUser(pack.getUser());
                break;
            }
            case BroadcastUserLeft:{
                BroadcastUserLeftPack pack = new BroadcastUserLeftPack(data);
                this.client.getUserManager().removeUser(pack.getUid());
                break;
            }
            case BroadcastUserNameChanged:{
                BroadcastUsernameChangedPack pack = new BroadcastUsernameChangedPack(data);
                this.client.getUserManager().changeUsername(pack.getUid(),pack.getName());
                break;
            }
            case FileUploadRequest: {
                UploadRequestPack pack = new UploadRequestPack(data);
                boolean ok;
                String reason;
                if (pack.getFileSize() > Config.fileMaxSize) {
                    ok = false;
                    reason = "File too large!";
                } else {
                    try{
                        ClientFileReceiveTask task = client.getFactoryManager().getFileReceiveTaskFactory().find(pack.getReceiverTaskId());
                        try{
                            task.setFileSize(pack.getFileSize());
                            ok = true;
                            reason = "";
                        }catch (IOException e){
                            ok = false;
                            reason = "Cannot create file on disk.";
                        }
                    }catch (NoSuchTaskIdException e){
                        client.getLogger().log(
                                Level.WARNING,
                                String.format("Client received a FileUploadRequest for {receiverTaskId=%s}, but it is not found in client.", pack.getReceiverTaskId())
                        );
                        ok = false;
                        reason = "No such task.";
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
                try{
                    ClientFileSendTask task = client.getFactoryManager().getFileSendTaskFactory().find(pack.getSenderTaskId());
                    task.onReceiveUploadReply(pack);
                }catch (NoSuchTaskIdException e){
                    client.getLogger().log(
                            Level.WARNING,
                            String.format("Client received a FileUploadReplyPack for {senderTaskId=%s}, but it is not found on client.", pack.getSenderTaskId())
                    );
                }
                break;
            }
            case FileUploadFinish: {
                UploadFinishPack pack = new UploadFinishPack(data);
                try{
                    ClientFileReceiveTask task = client.getFactoryManager().getFileReceiveTaskFactory().find(pack.getReceiverTaskId());
                    task.end();
                }catch (NoSuchTaskIdException e){
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
                try{
                    ClientFileSendTask task = client.getFactoryManager().getFileSendTaskFactory().find(pack.getSenderTaskId());
                    if (pack.isOk()) {
                        task.onEndSucceed();
                    } else {
                        task.onEndFailed(pack.getReason());
                    }
                }catch (NoSuchTaskIdException e){
                    client.getLogger().log(
                            Level.WARNING,
                            String.format("Client received a FileUploadResult for {senderTaskId=%s}, but it is not found in client.", pack.getSenderTaskId())
                    );
                }
                break;
            }
            case FileDownloadReply: {
                DownloadReplyPack pack = new DownloadReplyPack(data);
                try{
                    ClientFileReceiveTask task = client.getFactoryManager().getFileReceiveTaskFactory().find(pack.getReceiverTaskId());
                    if (pack.isOk()) {
                        task.setSenderTaskId(pack.getSenderTaskId());
                    } else {
                        task.onEndFailed(pack.getReason());
                        this.client.showInfo(String.format("Download failed: %s", pack.getReason()));
                    }
                }catch (NoSuchTaskIdException e){
                    client.getLogger().log(
                            Level.WARNING,
                            String.format("Client received a FileDownloadReplyPack for {receiverTaskId=%s}, but it is not found in client.", pack.getReceiverTaskId())
                    );
                }
                break;
            }
            case FileContent: {
                FileContentPack pack = new FileContentPack(data);
                try{
                    ClientFileReceiveTask task = client.getFactoryManager().getFileReceiveTaskFactory().find(pack.getReceiverTaskId());
                    try {
                        task.onDataReceived(pack.getData());
                    } catch (IOException e) {
                        task.end();
                    }
                }catch (NoSuchTaskIdException e){
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

    public void onConnectionBuilt(){
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

}
