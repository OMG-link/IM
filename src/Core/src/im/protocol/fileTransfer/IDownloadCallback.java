package im.protocol.fileTransfer;

public interface IDownloadCallback {
    void onSucceed(ClientFileReceiveTask task);
    void onFailed(ClientFileReceiveTask task,String reason);
}
