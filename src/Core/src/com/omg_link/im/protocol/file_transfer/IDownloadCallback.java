package com.omg_link.im.protocol.file_transfer;

public interface IDownloadCallback {
    void onSucceed(ClientFileReceiveTask task);
    void onFailed(ClientFileReceiveTask task,String reason);
}
