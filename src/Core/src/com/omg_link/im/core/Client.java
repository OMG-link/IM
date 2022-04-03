package com.omg_link.im.core;

import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.config.ConfigSetFailedException;
import com.omg_link.im.core.config.InvalidUserNameException;
import com.omg_link.im.core.factory_manager.ClientFactoryManager;
import com.omg_link.im.core.file_manager.ClientFileManager;
import com.omg_link.im.core.gui.*;
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

public class Client {
    private final Logger logger = Logger.getLogger("IMClient");
    private final ClientFactoryManager factoryManager = new ClientFactoryManager();
    private final ClientUserManager userManager = new ClientUserManager(this);
    private ClientFileManager fileManager;
    private ClientNetworkHandler networkHandler;

    private IConnectFrame connectFrame;
    private IRoomFrame roomFrame;
    private IGUI GUI;

    public Client(IGUI GUI) {
        try {
            this.GUI = GUI;
            this.fileManager = new ClientFileManager();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Unable to create server instance: " + e);
            System.exit(0);
        }
    }

    public void start() {
        createConnectFrame();
    }

    public void createConnectFrame() {
        getGUI().createConnectFrame();
    }

    public void createRoomFrame() {
        getGUI().createRoomFrame();
    }

    /**
     * 异步实现
     */
    public void runNetworkHandler() {
        final Client client = this;
        new Thread(() -> {
            if (client.networkHandler != null) {
                client.networkHandler.interrupt();
            }
            client.networkHandler = new ClientNetworkHandler(client, Config.getServerIP(), Config.getServerPort());
            client.networkHandler.connect();
            //Send CheckVersionPack
            try {
                client.getNetworkHandler().send(new CheckVersionPackV2());
            } catch (PackageTooLargeException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * Called when version check is done.
     */
    public void onConnectionBuilt() {
        try {
            //Call onConnectionBuilt
            IRoomFrame roomFrame;
            while ((roomFrame = getRoomFrame()) == null) {
                Thread.sleep(1); //In case of room frame is not built, we should wait.
            }
            roomFrame.onConnectionBuilt();
        } catch (InterruptedException e) {
            throw new RuntimeException(e); //Obviously, I won't interrupt it.
        }
    }

    public boolean setConfigAndStart(String url, String username, String token, boolean shouldRunLocalServer) throws InvalidUserNameException {
        //config
        try {
            Config.setUrl(url);
            getUserManager().getCurrentUser().setNameByInput(username);
            Config.setToken(token);
            Config.saveToFile();
        } catch (InvalidUserNameException e) {
            throw e;
        } catch (ConfigSetFailedException e) {
            this.showInfo(e.getMessage());
            return false;
        }
        //server
        if (shouldRunLocalServer) {
            Config.setServerIP("127.0.0.1");
            new Server(null);
        }
        //connect
        createRoomFrame();
        runNetworkHandler();
        return true;
    }

    //send messages

    public boolean sendChat(String message) {
        return sendChat(message, null);
    }

    public boolean sendChat(String message, IMessageSendCallback callback) {
        if (message.length() == 0) return false;

        try {
            getNetworkHandler().getMessageManager().sendChatText(message, callback);
        } catch (PackageTooLargeException e) {
            showInfo("Your message is too long!");
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

    //show messages

    public void showInfo(String message) {
        getGUI().showMessageDialog(message);
    }

    public void showCheckBox(String message, IConfirmDialogCallback callback) {
        getGUI().showConfirmDialog(message, callback);
    }

    public void showException(Exception e) {
        getGUI().showException(e);
    }

    //getter and setter

    public Logger getLogger() {
        return logger;
    }

    public ClientUserManager getUserManager() {
        return userManager;
    }

    public ClientFactoryManager getFactoryManager() {
        return factoryManager;
    }

    public ClientMessageManager getMessageManager() {
        return getNetworkHandler().getMessageManager();
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

    public void setRoomFrame(IRoomFrame roomFrame) {
        this.roomFrame = roomFrame;
    }

    public ClientFileManager getFileManager() {
        return fileManager;
    }

    public IGUI getGUI() {
        return GUI;
    }

}
