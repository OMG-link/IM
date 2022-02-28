package GUI;

import java.io.File;

public interface IGUI {
    void createConnectFrame();

    /**
     *  此方法的实现应当调用 Active Client 的 setRoomFrame方法。
     *  如此设计的原因是创建界面的过程可能是异步的。
     */
    void createRoomFrame();

    void showMessageDialog(String message);
    void showConfirmDialog(String message,IConfirmDialogCallback callback);
    void showException(Exception e);

    void openInBrowser(String uri);
    void onFileDownloaded(File file);
    void alertVersionMismatch(String serverVersion, String clientVersion);
    void alertVersionIncompatible(String serverVersion, String clientVersion);

}
