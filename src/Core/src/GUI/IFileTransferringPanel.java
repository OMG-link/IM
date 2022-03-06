package GUI;

import java.io.File;

public interface IFileTransferringPanel {
    void setProgress(long downloadedSize);
    void onTransferStart();
    void onTransferSucceed(File file);
    void onTransferFailed(String reason);
}
