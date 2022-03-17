package im;

import im.config.Config;
import im.config.ConfigSetFailedException;
import im.config.InvalidUserNameException;
import im.factory_manager.ClientFactoryManager;
import im.file_manager.ClientFileManager;
import im.gui.*;
import im.protocol.ClientNetworkHandler;
import im.protocol.data.PackageTooLargeException;
import im.protocol.data_pack.chat.ChatImagePack;
import im.protocol.data_pack.chat.TextPack;
import im.protocol.data_pack.file_transfer.DownloadRequestPack;
import im.protocol.data_pack.file_transfer.FileTransferType;
import im.protocol.data_pack.system.CheckVersionPack;
import im.protocol.fileTransfer.*;
import im.user_manager.ClientUserManager;
import mutils.ImageType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client{
    private final Logger logger = Logger.getLogger("IMClient");
    private final ClientFactoryManager factoryManager = new ClientFactoryManager();
    private final ClientUserManager userManager = new ClientUserManager(this);
    private ClientFileManager fileManager;
    private ClientNetworkHandler networkHandler;

    private IConnectFrame connectFrame;
    private IRoomFrame roomFrame;
    private IGUI GUI;

    public Client(IGUI GUI){
        try{
            this.GUI = GUI;
            this.fileManager = new ClientFileManager();
        }catch (Exception e){
            Logger.getGlobal().log(Level.SEVERE,"Unable to create server instance: "+e);
            System.exit(0);
        }
    }

    public void start(){
        createConnectFrame();
    }

    public void createConnectFrame(){
        getGUI().createConnectFrame();
    }

    public void createRoomFrame(){
        getGUI().createRoomFrame();
    }

    /**
     * 异步实现
     */
    public void runNetworkHandler(){
        final Client client = this;
        new Thread(() -> {
            if(client.networkHandler!=null){
                client.networkHandler.interrupt();
            }
            client.networkHandler = new ClientNetworkHandler(client, Config.getServerIP(), Config.getServerPort());
            client.networkHandler.connect();
            //Send CheckVersionPack
            try{
                client.getNetworkHandler().send(new CheckVersionPack());
            }catch (PackageTooLargeException e){
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * Called when version check is done.
     * SEND NOTHING BEFORE VERSION IS CHECKED!
     */
    public void onConnectionBuilt(){
        try{
            //Call onConnectionBuilt
            IRoomFrame roomFrame;
            while((roomFrame=getRoomFrame())==null){
                Thread.sleep(1); //In case of room frame is not built, we should wait.
            }
            roomFrame.onConnectionBuilt();
        }catch (InterruptedException e){
            throw new RuntimeException(e); //Obviously, I won't interrupt it.
        }
    }

    public boolean setConfigAndStart(String url,String username,String token,boolean shouldRunLocalServer) throws InvalidUserNameException {
        //config
        try{
            Config.setUrl(url);
            getUserManager().getCurrentUser().setNameByInput(username);
            Config.setToken(token);
            Config.saveToFile();
        }catch (InvalidUserNameException e){
            throw e;
        }catch (ConfigSetFailedException e){
            this.showInfo(e.getMessage());
            return false;
        }
        //server
        if(shouldRunLocalServer){
            Config.setServerIP("127.0.0.1");
            Server server = new Server(null);
            server.start();
        }
        //connect
        createRoomFrame();
        runNetworkHandler();
        return true;
    }

    //send messages

    public boolean sendChat(String message){
        if(message.length()==0) return false;
        TextPack textPack = new TextPack(message);

        try{
            getNetworkHandler().send(textPack);
        }catch (PackageTooLargeException e){
            showInfo("Your message is too long!");
            return false;
        }
        return true;
    }

    public void sendChatImage(File image, ImageType imageType) throws FileNotFoundException {
        uploadFile(image, FileTransferType.ChatImage, null, new IUploadCallback() {
            @Override
            public void onSucceed(ClientFileSendTask task) {
                ChatImagePack pack = new ChatImagePack(task.getReceiverFileId(),imageType);
                try{
                    getNetworkHandler().send(pack);
                }catch (PackageTooLargeException e){
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailed(ClientFileSendTask task,String reason) {
                showInfo(String.format("Failed to upload chat image: %s",reason));
            }

        });
    }

    public FileSendTask uploadFile(File file, FileTransferType fileTransferType, IFileTransferringPanel panel, IUploadCallback callback) throws FileNotFoundException {
        FileSendTask task = getFactoryManager().getFileSendTaskFactory().create(this, file, fileTransferType, panel, callback);
        task.start();
        return task;
    }

    public FileSendTask uploadFile(File file, FileTransferType fileTransferType, IFileTransferringPanel panel) throws FileNotFoundException {
        return uploadFile(file,fileTransferType,panel,null);
    }

    public void downloadFile(String fileName, UUID fileId, FileTransferType fileTransferType, IFileTransferringPanel panel, IDownloadCallback callback){
        try{
            FileReceiveTask task = getFactoryManager().getFileReceiveTaskFactory().create(this, fileName, fileId, fileTransferType, panel, callback);
            getNetworkHandler().send(new DownloadRequestPack(
                    task.getReceiverTaskId(),
                    task.getSenderFileId(),
                    task.getReceiverFileId(),
                    fileTransferType
            ));
        }catch (IOException e){
            showInfo("Cannot create file on disk.");
        }catch (PackageTooLargeException e){
            //This should never happen!
            showInfo("Unable to send download request.");
        }
    }

    public void downloadFile(String fileName, UUID fileId,FileTransferType fileTransferType, IFileTransferringPanel panel){
        downloadFile(fileName, fileId,fileTransferType,panel, ClientFileReceiveTask.getDefaultCallback(fileTransferType));
    }

    //show messages

    public void showInfo(String message){
        getGUI().showMessageDialog(message);
    }

    public void showCheckBox(String message, IConfirmDialogCallback callback){
        getGUI().showConfirmDialog(message,callback);
    }

    public void showException(Exception e){
        getGUI().showException(e);
    }

    //getter and setter

    public Logger getLogger(){
        return logger;
    }

    public ClientUserManager getUserManager() {
        return userManager;
    }

    public ClientFactoryManager getFactoryManager() {
        return factoryManager;
    }

    public ClientNetworkHandler getNetworkHandler() {
        return networkHandler;
    }

    public IConnectFrame getConnectFrame() {
        return connectFrame;
    }

    public void setConnectFrame(IConnectFrame connectFrame) {
        this.connectFrame = connectFrame;
    }

    public IRoomFrame getRoomFrame() {
        return roomFrame;
    }

    public void setRoomFrame(IRoomFrame roomFrame){
        this.roomFrame = roomFrame;
    }

    public ClientFileManager getFileManager() {
        return fileManager;
    }

    public IGUI getGUI() {
        return GUI;
    }

}
