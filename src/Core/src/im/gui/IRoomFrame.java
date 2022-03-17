package im.gui;

import im.protocol.fileTransfer.IDownloadCallback;
import im.user_manager.User;
import mutils.IStringGetter;

import java.util.Collection;
import java.util.UUID;

public interface IRoomFrame {

    void exitRoom(String reason);

    /**
     * Called when connection to the server has been built.
     * This function should allow user to type texts and so on.
     */
    void onConnectionBuilt();
    void onConnectionBroke();

    void showSystemMessage(String message);
    void showTextMessage(String sender, long stamp, String text);
    void showFileUploadedMessage(String sender, long stamp, UUID uuid, String fileName, long fileSize);

    //This function is not written in the traditional way, but I don't want to fix it now.
    /**
     * Called when chat image pack was received.
     * This function should create a chat image panel, and return the callback function which is called when image is downloaded.
     * @param sender Message sender.
     * @param stamp The time when the message was sent.
     * @param serverFileId The id of image in the server.
     * @return Return the callback function which is called when image is downloaded.
     */
    IDownloadCallback showChatImageMessage(String sender, long stamp, UUID serverFileId);

    void onRoomNameUpdate(String roomName);
    IFileTransferringPanel addFileTransferringPanel(IStringGetter fileNameGetter,long fileSize);

    void updateUserList(Collection<User> userList);
    void onUserJoined(User user);
    void onUserLeft(User user);
    void onUsernameChanged(User user,String previousName);

}
