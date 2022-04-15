package com.omg_link.im.core;

import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.config.ConfigSetFailedException;
import com.omg_link.im.core.factory_manager.ClientFactoryManager;
import com.omg_link.im.core.file_manager.ClientFileManager;
import com.omg_link.im.core.file_manager.NoSuchFileIdException;
import com.omg_link.im.core.gui.IConfirmDialogCallback;
import com.omg_link.im.core.gui.IFileTransferringPanel;
import com.omg_link.im.core.gui.IRoomFrame;
import com.omg_link.im.core.message_manager.ClientMessageManager;
import com.omg_link.im.core.protocol.ClientNetworkHandler;
import com.omg_link.im.core.protocol.data.PackageTooLargeException;
import com.omg_link.im.core.protocol.data_pack.file_transfer.DownloadRequestPack;
import com.omg_link.im.core.protocol.file_transfer.FileTransferType;
import com.omg_link.im.core.protocol.data_pack.system.CheckVersionPackV2;
import com.omg_link.im.core.protocol.file_transfer.FileReceiveTask;
import com.omg_link.im.core.protocol.file_transfer.FileSendTask;
import com.omg_link.im.core.protocol.file_transfer.NoSuchTaskIdException;
import com.omg_link.im.core.sql_manager.client.ClientSqlManager;
import com.omg_link.im.core.user_manager.ClientUserManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientRoom {

    private final Client client;
    private final ClientFactoryManager factoryManager = new ClientFactoryManager();
    private final ClientUserManager userManager = new ClientUserManager(this);
    private ClientNetworkHandler networkHandler;

    private UUID serverId;
    private ClientSqlManager sqlManager;
    private ClientMessageManager messageManager;

    private IRoomFrame roomFrame = null;
    private boolean isConnectionBuilt = false;

    public ClientRoom(
            Client client
    ) throws ConfigSetFailedException {
        this.client = client;
    }

    /**
     * <p>Save the config and connect to server.</p>
     *
     * @throws ConfigSetFailedException When config is saved failed.
     */
    public void saveConfigAndConnect(String url, String username, String token) throws ConfigSetFailedException {
        // save config
        Config.setUrl(url);
        getUserManager().getCurrentUser().setNameByInput(username);
        Config.setToken(token);
        Config.saveToFile();
        // connect
        new Thread(() -> {
            var remoteServerIp = client.isLocalServerRunning() ? "127.0.0.1" : Config.getServerIP();
            networkHandler = new ClientNetworkHandler(this, remoteServerIp, Config.getServerPort());
            networkHandler.connect();
            getNetworkHandler().send(new CheckVersionPackV2());
        }).start();
    }

    public void reconnect() {
        networkHandler.stop();
        try {
            saveConfigAndConnect(Config.getUrl(), Config.getUsername(), Config.getToken());
        } catch (ConfigSetFailedException e) {
            throw new RuntimeException(e);
        }
    }

    public void exitRoom(IRoomFrame.ExitReason state) {
        networkHandler.stop();
        roomFrame.exitRoom(state);
        client.setRoom(null);
    }

    /**
     * Called when handshake is done.
     */
    public void onConnectionBuilt() {
        isConnectionBuilt = true;
        if (roomFrame != null) {
            roomFrame.onConnectionBuilt();
        }
    }

    //send messages

    public boolean sendChat(String message) {
        if (message.length() == 0) return false;

        try {
            getMessageManager().sendChatText(message);
        } catch (PackageTooLargeException e) {
            client.showMessage("Your message is too long!");
            return false;
        }
        return true;
    }

    public void sendChatImage(File image) throws FileNotFoundException {
        sendChatImage(image, null);
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

    // Maybe this function should be moved into ClientFileManager?
    public void downloadFile(String fileName, UUID fileId, FileTransferType fileTransferType, IFileTransferringPanel panel) {
        if (fileTransferType == FileTransferType.ChatImage) {
            if (getFileManager().isFileDownloaded(fileId)) {
                try {
                    panel.onTransferSucceed(getFileManager().openFileByServerFileId(fileId));
                    return;
                } catch (FileNotFoundException ignored) { //吊人搞我
                }
            } else if (getFileManager().isFileDownloading(fileId)) {
                try {
                    getFileManager().addFileDownloadCallback(fileId, panel);
                    return;
                } catch (NoSuchFileIdException | NoSuchTaskIdException e) { //吊人搞我
                    throw new RuntimeException(e);
                }
            }
        }
        FileReceiveTask task;
        try {
            task = getFactoryManager().getFileReceiveTaskFactory().create(this, fileName, fileId, fileTransferType, panel);
        } catch (IOException e) {
            panel.onTransferFailed("Cannot create file on disk.");
            return;
        }
        getNetworkHandler().send(new DownloadRequestPack(
                task.getReceiverTaskId(),
                task.getSenderFileId(),
                task.getReceiverFileId(),
                fileTransferType
        ));
    }

    // show info

    public void showMessage(String message) {
        client.showMessage(message);
    }

    public void showCheckbox(String message, IConfirmDialogCallback callback) {
        client.showCheckbox(message, callback);
    }

    // version error

    public void alertVersionUnrecognizable() {
        client.getGui().alertVersionUnrecognizable(Config.version);
    }

    public void alertVersionIncompatible(String remoteVersion) {
        client.getGui().alertVersionIncompatible(remoteVersion, Config.version);
    }

    public void alertVersionMismatch(String remoteVersion) {
        client.getGui().alertVersionMismatch(remoteVersion, Config.version);
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
        if (this.roomFrame != null) {
            client.getLogger().log(
                    Level.WARNING,
                    "Room frame set more than once! The latter operation will be ignored."
            );
            return;
        }
        this.roomFrame = roomFrame;
        if (isConnectionBuilt) {
            onConnectionBuilt();
        }
    }

    public ClientFactoryManager getFactoryManager() {
        return factoryManager;
    }

    public ClientFileManager getFileManager() {
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

    public ClientSqlManager getSqlManager() {
        return sqlManager;
    }

    //setter

    public void setServerId(UUID serverId, long lastMessageSerialId) {
        this.serverId = serverId;
        try {
            this.sqlManager = new ClientSqlManager(
                    client.getGui().getSqlComponentFactory(),
                    "{cache}/chatLog.db"
                            .replace("{cache}", ClientFileManager.getCacheFolderName(serverId))
                            .replace("{serverId}", serverId.toString())
                    , serverId
            );
        } catch (SQLException e) {
            e.printStackTrace();
            this.sqlManager = null;
        }
        boolean isSqlEnabled = sqlManager != null;

        getFileManager().setToRoom(this, serverId, isSqlEnabled);
        this.messageManager = new ClientMessageManager(this, lastMessageSerialId, isSqlEnabled);
    }

    public void closeSqlManager() {
        if (sqlManager != null) {
            sqlManager.close();
            this.sqlManager = null;
        }
    }

}
