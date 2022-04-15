package com.omg_link.im.core.gui;

import java.util.UUID;

public interface IFileTransferringPanel {
    default void setProgress(long downloadedSize){}
    default void onTransferStart(){}
    default void onTransferSucceed(UUID senderFileId, UUID receiverFileId){}
    default void onTransferFailed(String reason){}
}
