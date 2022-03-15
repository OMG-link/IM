package im.gui;

import im.protocol.fileTransfer.IDownloadCallback;
import im.user_manager.User;
import mutils.IStringGetter;

import java.util.Collection;
import java.util.UUID;

public interface IRoomFrame {

    /**
     * Called when connection to the server has been built.
     * This function should allow user to type texts and so on.
     */
    void onConnectionBuilt();

    void setVisible(boolean b);

    void clearMessageArea();

    void onMessageReceive(String sender,long stamp,String text);

    /**
     * Called when chat image pack was received.
     * This function should create a chat image panel, and return the callback function which is called when image is downloaded.
     * @param sender Message sender.
     * @param stamp The time when the message was sent.
     * @param serverFileId The id of image in the server.
     * @return Return the callback function which is called when image is downloaded.
     */
    IDownloadCallback onChatImageReceive(String sender, long stamp, UUID serverFileId);

    void onFileUploadedReceive(String sender, long stamp, UUID uuid, String fileName, long fileSize);
    void onUserListUpdate(Collection<User> userList);
    void onRoomNameUpdate(String roomName);
    IFileTransferringPanel addFileTransferringPanel(IStringGetter fileNameGetter,long fileSize);

}
