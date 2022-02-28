package protocol.helper.fileTransfer;

public interface IDownloadCallback {
    void onSucceed(ClientFileReceiveTask task);
    void onFailed(ClientFileReceiveTask task);
}
