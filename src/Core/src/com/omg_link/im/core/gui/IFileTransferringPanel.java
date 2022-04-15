package com.omg_link.im.core.gui;

import com.omg_link.im.core.file_manager.FileObject;

public interface IFileTransferringPanel {
    default void setProgress(long downloadedSize){}
    default void onTransferStart(){}
    default void onTransferSucceed(FileObject fileObject){}
    default void onTransferFailed(String reason){}
}
