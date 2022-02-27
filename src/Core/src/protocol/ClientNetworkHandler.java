package protocol;

import IM.Client;
import IM.Config;
import mutil.file.WriteOnlyFile;
import mutil.uuidLocator.UuidConflictException;
import protocol.dataPack.*;
import protocol.helper.data.Data;
import protocol.helper.data.InvalidPackageException;
import protocol.helper.data.PackageTooLargeException;
import protocol.helper.fileTransfer.ClientFileReceiveTask;
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

    public void interrupt(){
        if(isInterrupted) return;
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
                Data lengthBuffer = new Data(this.inputStream, 4);
                int length = Data.decodeInt(lengthBuffer);
                if (length > Config.packageMaxLength)
                    throw new InvalidPackageException();
                Data data = new Data(this.inputStream, length);
                this.onRecv(data);
            }
        } catch (IOException e) {
            //If IOException is caused by interrupt, just return.
            if(this.isInterrupted){
                Logger.getLogger("IMCore").log(Level.INFO,"thread interrupted");
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

    public synchronized void send(Data data) throws PackageTooLargeException {
        if (data.length() > Config.packageMaxLength)
            throw new PackageTooLargeException();

        Data rawData = new Data();
        rawData.append(new Data(data.length()));
        rawData.append(data);
        try {
            outputStream.write(rawData.getData());
            outputStream.flush();
        } catch (IOException e) {
            this.handler.showInfo("Connection Error!");
            this.close();
        }
    }

    private void onRecv(Data data) throws InvalidPackageException {
        DataPackType type = DataPackType.toType(Data.peekInt(data));
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
                        pack.getUuid(),
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
                    this.handler.getGUI().alertVersionError(pack.getVersion(),Config.version);
                }
                break;
            }
            case FileUploadReply: {
                UploadReplyPack pack = new UploadReplyPack(data);
                FileSendTask task = (FileSendTask) this.handler.getUuidManager().get(pack.getUuid());
                task.onReceiveUploadReply(pack.isOk(), pack.getDesc());
                break;
            }
            case FileDownloadReply: {
                DownloadReplyPack pack = new DownloadReplyPack(data);
                if (!pack.isOk()) {
                    this.handler.showInfo(String.format("Download failed: %s", pack.getReason()));
                }
                break;
            }
            case FileUploadRequest: {
                UploadRequestPack pack = new UploadRequestPack(data);
                UploadReplyPack replyPack;
                if (pack.getFileSize() > Config.fileMaxSize) {
                    replyPack = new UploadReplyPack(
                            pack.getUuid(),
                            false,
                            "File too large!"
                    );
                } else {
                    try {
                        WriteOnlyFile file = handler.getFileManager().createFileRenameable(
                                pack.getUuid(),
                                pack.getFileName(),
                                pack.getFileSize()
                        ).getWriteOnlyInstance();
                        ClientFileReceiveTask task = new ClientFileReceiveTask(
                                handler,
                                file,
                                pack.getUuid(),
                                pack.getFileSize()
                        );
                        task.start();
                        replyPack = new UploadReplyPack(
                                pack.getUuid(),
                                true,
                                ""
                        );
                    } catch (UuidConflictException e) {
                        replyPack = new UploadReplyPack(
                                pack.getUuid(),
                                false,
                                "Unexpected UUID conflict."
                        );
                    } catch (IOException e) {
                        replyPack = new UploadReplyPack(
                                pack.getUuid(),
                                false,
                                "Unable to create target file."
                        );
                    }
                }
                try {
                    this.send(replyPack);
                } catch (PackageTooLargeException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case FileContent: {
                FileContentPack pack = new FileContentPack(data);
                Object o = handler.getUuidManager().get(pack.getUuid());
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
                }
                break;
            }
            default: {
                Logger.getGlobal().log(Level.WARNING, "Client was unable to decode package type: " + type);
                throw new InvalidPackageException();
            }
        }
    }
}
