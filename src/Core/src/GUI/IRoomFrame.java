package GUI;

import java.util.UUID;

public interface IRoomFrame {

    void setVisible(boolean b);

    void clearMessageArea();

    void onMessageReceive(String sender,long stamp,String text);
    void onFileUploadedReceive(String sender, long stamp, UUID uuid, String fileName, long fileSize);
    void onUserListUpdate(String[] userList);
    IFileTransferringPanel addFileTransferringPanel(String fileName);

}
