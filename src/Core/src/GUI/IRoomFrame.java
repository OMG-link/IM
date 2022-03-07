package GUI;

import mutils.IStringGetter;
import protocol.helper.fileTransfer.IDownloadCallback;

import java.util.UUID;

public interface IRoomFrame {

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
    void onUserListUpdate(String[] userList);
    IFileTransferringPanel addFileTransferringPanel(IStringGetter fileNameGetter,long fileSize);

}
