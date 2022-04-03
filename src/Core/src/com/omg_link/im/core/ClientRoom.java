package com.omg_link.im.core;

import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.config.ConfigSetFailedException;
import com.omg_link.im.core.factory_manager.ClientFactoryManager;
import com.omg_link.im.core.file_manager.ClientFileManager;
import com.omg_link.im.core.gui.IConfirmDialogCallback;
import com.omg_link.im.core.gui.IFileTransferringPanel;
import com.omg_link.im.core.gui.IRoomFrame;
import com.omg_link.im.core.message_manager.ClientMessageManager;
import com.omg_link.im.core.protocol.ClientNetworkHandler;
import com.omg_link.im.core.protocol.data.PackageTooLargeException;
import com.omg_link.im.core.protocol.data_pack.chat.IMessageSendCallback;
import com.omg_link.im.core.protocol.data_pack.file_transfer.DownloadRequestPack;
import com.omg_link.im.core.protocol.data_pack.file_transfer.FileTransferType;
import com.omg_link.im.core.protocol.data_pack.system.CheckVersionPackV2;
import com.omg_link.im.core.protocol.file_transfer.FileReceiveTask;
import com.omg_link.im.core.protocol.file_transfer.FileSendTask;
import com.omg_link.im.core.user_manager.ClientUserManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientRoom {

    private final Client client;
    private final ClientMessageManager messageManager = new ClientMessageManager(this);
    private final ClientFactoryManager factoryManager = new ClientFactoryManager();
    private final ClientUserManager userManager = new ClientUserManager(this);
    private ClientNetworkHandler networkHandler;

    private IRoomFrame roomFrame = null;
    private boolean isConnectionBuilt = false;

    public ClientRoom(
            Client client
    ) throws ConfigSetFailedException {
        this.client = client;
    }

    /**
     * <p>Save the config and connect to server.</p>
     * @throws ConfigSetFailedException When config is saved failed.
     */
    public void saveConfigAndConnect(String url, String username, String token) throws ConfigSetFailedException {
        // save config
        Config.setUrl(url);
        getUserManager().getCurrentUser().setNameByInput(username);
        Config.setToken(token);
        Config.saveToFile();
        // connect
        new Thread(()->{
            var remoteServerIp = client.isLocalServerRunning()?"127.0.0.1":Config.getServerIP();
            networkHandler = new ClientNetworkHandler(this, remoteServerIp, Config.getServerPort());
            networkHandler.connect();
            getNetworkHandler().send(new CheckVersionPackV2());
        }).start();
    }

    public void reconnect(){
        networkHandler.stop();
        try {
            saveConfigAndConnect(Config.getUrl(),Config.getUsername(), Config.getToken());
        } catch (ConfigSetFailedException e) {
            throw new RuntimeException(e);
        }
    }

    public void exitRoom(IRoomFrame.ExitReason reason){
        networkHandler.stop();
        roomFrame.exitRoom(reason);
        client.setRoom(null);
    }

    /**
     * Called when handshake is done.
     */
    public void onConnectionBuilt() {
        isConnectionBuilt = true;
        if(roomFrame!=null){
            roomFrame.onConnectionBuilt();
        }
    }

    //send messages

    public boolean sendChat(String message) {
        return sendChat(message, null);
    }

    public boolean sendChat(String message, IMessageSendCallback callback) {
        if (message.length() == 0) return false;

        try {
            getMessageManager().sendChatText(message, callback);
        } catch (PackageTooLargeException e) {
            client.showMessage("Your message is too long!");
            return false;
        }
        return true;
    }

    public void sendChatImage(File image) throws FileNotFoundException {
        sendChatImage(image,null);
    }

    public void sendChatImage(File image, IFileTransferringPanel panel) throws FileNotFoundException {
        uploadFile(image, FileTransferType.ChatImage, panel);
    }

    public FileSendTask uploadFile(File file, IFileTransferringPanel panel) throws FileNotFoundException {
        return uploadFile(file, FileTransferType.ChatFile, panel);
    }

    private FileSendTask uploadFile(File file, FileTransferType fileTransferType, IFileTransferringPanel panel) throws FileNotFoundException {
        FileSendTask task = getFactoryManager().getFileSendTaskFactory().create(this, file, fileTransferType, panel);
        task.start();
        return task;
    }

    public void downloadFile(String fileName, UUID fileId, FileTransferType fileTransferType, IFileTransferringPanel panel) {
        FileReceiveTask task;
        try {
            task = getFactoryManager().getFileReceiveTaskFactory().create(this, fileName, fileId, fileTransferType, panel);
        } catch (IOException e) {
            panel.onTransferFailed("Cannot create file on disk.");
            return;
        }
        try {
            getNetworkHandler().send(new DownloadRequestPack(
                    task.getReceiverTaskId(),
                    task.getSenderFileId(),
                    task.getReceiverFileId(),
                    fileTransferType
            ));
        } catch (PackageTooLargeException e) { //This should never happen!
            task.onEndFailed("Unable to send download request.");
        }
    }

    // show info

    public void showMessage(String message){
        client.showMessage(message);
    }

    public void showCheckbox(String message, IConfirmDialogCallback callback){
        client.showCheckbox(message,callback);
    }

    // version error

    public void alertVersionUnrecognizable(){
        client.getGui().alertVersionUnrecognizable(Config.version);
    }

    public void alertVersionIncompatible(String remoteVersion){
        client.getGui().alertVersionIncompatible(remoteVersion,Config.version);
    }

    public void alertVersionMismatch(String remoteVersion){
        client.getGui().alertVersionMismatch(remoteVersion,Config.version);
    }

    // getter

    public Client getClient() {
        return client;
    }

    public Logger getLogger() {
        return client.getLogger();
    }

    public IRoomFrame getRoomFrame() {
        return roomFrame;
    }

    public void setRoomFrame(IRoomFrame roomFrame) {
        if(this.roomFrame!=null){
            client.getLogger().log(
                    Level.WARNING,
                    "Room frame set more than once! The latter operation will be ignored."
            );
            return;
        }
        this.roomFrame = roomFrame;
        if(isConnectionBuilt){
            onConnectionBuilt();
        }
    }

    public ClientFactoryManager getFactoryManager() {
        return factoryManager;
    }

    public ClientFileManager getFileManager(){
        return client.getFileManager();
    }

    public ClientMessageManager getMessageManager() {
        return messageManager;
    }

    public ClientNetworkHandler getNetworkHandler() {
        return networkHandler;
    }

    public ClientUserManager getUserManager() {
        return userManager;
    }

}
