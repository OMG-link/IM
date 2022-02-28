package protocol.helper.fileTransfer;

public interface IUploadCallback {
    void onSucceed(ClientFileSendTask task);
    void onFailed(ClientFileSendTask task,String reason);
}
