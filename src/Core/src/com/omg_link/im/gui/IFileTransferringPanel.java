package com.omg_link.im.gui;

import com.omg_link.im.file_manager.FileObject;

public interface IFileTransferringPanel {
    void setProgress(long downloadedSize);
    void onTransferStart();
    void onTransferSucceed(FileObject fileObject);
    void onTransferFailed(String reason);
}
