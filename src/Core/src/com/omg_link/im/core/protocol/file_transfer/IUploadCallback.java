package com.omg_link.im.core.protocol.file_transfer;

public interface IUploadCallback {
    void onSucceed(ClientFileSendTask task);
    void onFailed(ClientFileSendTask task,String reason);
}
