package GUI;

import mutil.IStringGetter;
import protocol.dataPack.ImageType;
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
     * @param imageUUID UUID used for locate local file.
     * @param imageType Format of image, such as PNG, JPG.
     * @return Return the callback function which is called when image is downloaded.
     */
    IDownloadCallback onChatImageReceive(String sender, long stamp, UUID imageUUID, ImageType imageType);

    void onFileUploadedReceive(String sender, long stamp, UUID uuid, String fileName, long fileSize);
    void onUserListUpdate(String[] userList);
    IFileTransferringPanel addFileTransferringPanel(IStringGetter fileNameGetter);

}
