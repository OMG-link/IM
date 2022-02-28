package protocol;

import IM.Client;
import IM.Config;
import mutil.file.WriteOnlyFile;
import protocol.dataPack.*;
import protocol.helper.data.ByteData;
import protocol.helper.data.InvalidPackageException;
import protocol.helper.data.PackageTooLargeException;
import protocol.helper.fileTransfer.ClientFileReceiveTask;
import protocol.helper.fileTransfer.ClientFileSendTask;
import protocol.helper.fileTransfer.FileReceiveTask;
import protocol.helper.fileTransfer.FileSendTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientNetworkHandler implements Runnable {
    private final Client handler;
    private final String url;
    private final int port;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    private Timer pingTimer;

    private boolean isInterrupted = false;

    public void interrupt() {
        if (isInterrupted) return;
        isInterrupted = true;
        this.close();
    }

    public ClientNetworkHandler(Client handler, String url, int port) {
        this.handler = handler;
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

            pingTimer = new Timer();
            pingTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        handler.getNetworkHandler().send(new PingPack());
                    } catch (PackageTooLargeException e) {
                        throw new RuntimeException(e);
                    }
                }
            }, 5000, 10 * 1000);

        } catch (UnknownHostException e) {
            this.handler.showInfo("Unknown host!");
            System.exit(0);
        } catch (Exception e) {
            this.handler.showInfo(e.toString());
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void close() {
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
                if (length > Config.packageMaxLength)
                    throw new InvalidPackageException();
                ByteData data = new ByteData(this.inputStream, length);
                this.onRecv(data);
            }
        } catch (IOException e) {
            //If IOException is caused by interrupt, just return.
            if (this.isInterrupted) {
                Logger.getLogger("IMCore").log(Level.INFO, "thread interrupted");
                return;
            }
            //Otherwise, try auto reconnect.
            if (this.handler.getRoomFrame() != null) {
                this.handler.getRoomFrame().onMessageReceive(
                        "System",
                        System.currentTimeMillis(),
                        "Disconnected from the server, trying to reconnect."
                );
            }
            this.close();
            handler.runNetworkHandler();
        } catch (InvalidPackageException e) {
            e.printStackTrace();
            this.handler.showInfo("Client received an invalid package. Connection has been closed.");
            this.close();
            System.exit(0);
        } catch (Exception e) {
            this.handler.showException(e);
            this.close();
            System.exit(0);
        }
    }

    public void send(DataPack dataPack) throws PackageTooLargeException {
        this.send(dataPack.encode());
    }

    public synchronized void send(ByteData data) throws PackageTooLargeException {
        if (data.length() > Config.packageMaxLength)
            throw new PackageTooLargeException();

        ByteData rawData = new ByteData();
        rawData.append(new ByteData(data.length()));
        rawData.append(data);
        try {
            outputStream.write(rawData.getData());
            outputStream.flush();
        } catch (IOException e) {
            this.handler.showInfo("Connection Error!");
            this.close();
        }
    }

    private void onRecv(ByteData data) throws InvalidPackageException {
        DataPackType type = DataPackType.toType(ByteData.peekInt(data));
        switch (type) {
            case Text: {
                TextPack pack = new TextPack(data);
                this.handler.getRoomFrame().onMessageReceive(
                        pack.getSender(),
                        pack.getStamp(),
                        pack.getText()
                );
                break;
            }
            case FileUploaded: {
                FileUploadedPack pack = new FileUploadedPack(data);
                this.handler.getRoomFrame().onFileUploadedReceive(
                        pack.getSender(),
                        pack.getStamp(),
                        pack.getFileId(),
                        pack.getFileName(),
                        pack.getFileSize()
                );
                break;
            }
            case UserList: {
                UserListPack userListPack = new UserListPack(data);
                this.handler.getRoomFrame().onUserListUpdate(userListPack.getUserList());
                break;
            }
            case CheckVersion: {
                CheckVersionPack pack = new CheckVersionPack(data);

                if (!Objects.equals(pack.getVersion(), Config.version)) {
                    this.handler.getGUI().alertVersionMismatch(pack.getVersion(), Config.version);
                }
                break;
            }
            case FileUploadReply: {
                UploadReplyPack pack = new UploadReplyPack(data);
                Object o = this.handler.getUuidManager().get(pack.getSenderTaskId());
                if (o instanceof FileSendTask) {
                    FileSendTask task = (FileSendTask) o;
                    task.onReceiveUploadReply(pack.isOk(), pack.getDesc());
                    task.setReceiverTaskId(pack.getReceiverTaskId());
                } else {
                    Logger.getLogger("IMCore").log(
                            Level.WARNING,
                            String.format("Client received a FileUploadReplyPack for {senderTaskId=%s}, but it is not found in client.", pack.getSenderTaskId())
                    );
                }
                break;
            }
            case FileDownloadReply: {
                DownloadReplyPack pack = new DownloadReplyPack(data);
                Object o = this.handler.getUuidManager().get(pack.getReceiverTaskId());
                if (o instanceof FileReceiveTask) {
                    FileReceiveTask task = (FileReceiveTask) o;
                    if (!pack.isOk()) {
                        task.end(pack.getReason());
                        this.handler.showInfo(String.format("Download failed: %s", pack.getReason()));
                    }
                } else {
                    Logger.getLogger("IMCore").log(
                            Level.WARNING,
                            String.format("Client received a FileDownloadReplyPack for {receiverTaskId=%s}, but it is not found in client.", pack.getReceiverTaskId())
                    );
                }
                break;
            }
            case FileUploadRequest: {
                UploadRequestPack pack = new UploadRequestPack(data);
                boolean ok;
                String desc;
                if (pack.getFileSize() > Config.fileMaxSize) {
                    ok = false;
                    desc = "File too large!";
                } else {
                    try {
                        Object o = handler.getUuidManager().get(pack.getReceiverTaskId());
                        if (o instanceof ClientFileReceiveTask) {
                            ClientFileReceiveTask task = (ClientFileReceiveTask) o;
                            WriteOnlyFile file;
                            if (pack.getFileTransferType() == FileTransferType.ChatFile) {
                                file = handler.getFileManager().createFileRenameable(pack.getFileName()).getWriteOnlyInstance();
                            } else {
                                file = handler.getFileManager().createCacheFileRenameable(task.getReceiverTaskId().toString()).getWriteOnlyInstance();
                            }
                            task.setWriteOnlyFile(file);
                            task.setFileSize(pack.getFileSize());
                            task.setSenderTaskId(pack.getSenderTaskId());
                            ok = true;
                            desc = "";
                        } else {
                            ok = false;
                            desc = "No such task.";
                        }
                    } catch (IOException e) {
                        ok = false;
                        desc = "Unable to create target file.";
                    }
                }
                try {
                    this.send(new UploadReplyPack(
                            pack.getSenderTaskId(),
                            pack.getReceiverTaskId(),
                            ok,
                            desc,
                            pack.getFileTransferType()
                    ));
                } catch (PackageTooLargeException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case FileContent: {
                FileContentPack pack = new FileContentPack(data);
                Object o = handler.getUuidManager().get(pack.getReceiverTaskId());
                if (o instanceof FileReceiveTask) {
                    FileReceiveTask task = (FileReceiveTask) o;
                    try {
                        if (pack.getOffset() < 0) {
                            task.end();
                        } else {
                            task.onDataReceived(pack.getData());
                        }
                    } catch (IOException e) {
                        task.end();
                    }
                } else {
                    Logger.getLogger("IMCore").log(
                            Level.WARNING,
                            String.format("Client received a FileContent for {receiverTaskId=%s}, but it is not found in client.", pack.getReceiverTaskId())
                    );
                }
                break;
            }
            case ChatImage: {
                ChatImagePack pack = new ChatImagePack(data);
                var callback = handler.getRoomFrame().onChatImageReceive(pack.getSender(), pack.getStamp(), pack.getImageUUID(), pack.getImageType());
                handler.downloadFile(pack.getImageUUID(), FileTransferType.ChatImage, callback);
                break;
            }
            case FileUploadResult:{
                UploadResultPack pack = new UploadResultPack(data);
                Object o = handler.getUuidManager().get(pack.getSenderTaskId());
                if(o instanceof ClientFileSendTask){
                    ClientFileSendTask task = (ClientFileSendTask) o;
                    if(pack.isOk()){
                        task.setUploadedFileId(pack.getUploadedFileId());
                        task.onEndSucceed();
                    }else{
                        task.onEndFailed(pack.getReason());
                    }
                }else{
                    Logger.getLogger("IMCore").log(
                            Level.WARNING,
                            String.format("Client received a FileUploadResult for {senderTaskId=%s}, but it is not found in client.", pack.getSenderTaskId())
                    );
                }
                break;
            }
            default: {
                Logger.getGlobal().log(Level.WARNING, "Client was unable to handle package type: " + type);
                throw new InvalidPackageException();
            }
        }
    }
}
