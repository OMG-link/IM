package protocol.helper.fileTransfer;

import GUI.IFileTransferringPanel;
import IM.Client;
import mutils.file.ClientFileManager;
import mutils.file.FileObject;
import mutils.uuidLocator.UUIDManager;
import protocol.dataPack.DataPack;
import protocol.dataPack.FileTransferType;
import protocol.helper.data.PackageTooLargeException;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.UUID;

public class ClientFileReceiveTask extends FileReceiveTask {
    private final Client handler;
    private final IFileTransferringPanel panel;
    private final String fileName;
    private final IDownloadCallback callback;

    //constructors

    /**
     * @throws IOException - When the file cannot be created.
     */
    public ClientFileReceiveTask(
            Client handler,
            String fileName, UUID senderFileId, FileTransferType fileTransferType,
            IFileTransferringPanel panel, IDownloadCallback callback
    ) throws IOException {
        super(fileTransferType);
        this.handler = handler;
        this.fileName = fileName;
        this.panel = panel;
        this.callback = callback;
        super.setReceiverTaskId();
        super.setSenderFileId(senderFileId);

        FileObject fileObject = getFileManager().createFileRenameable(fileName);
        super.setReceiverFileId(fileObject.getFileId());
        super.setFileWriter(fileObject.getWriteOnlyInstance());

    }

    //abstract

    @Override
    protected void onTransferProgressChange(long downloadedSize) {
        if (getFileTransferType() != FileTransferType.ChatFile) return;
        panel.setProgress(downloadedSize);
    }

    @Override
    public UUIDManager getUuidManager() {
        return handler.getUuidManager();
    }

    @Override
    protected ClientFileManager getFileManager() {
        return handler.getFileManager();
    }

    @Override
    protected void send(DataPack dataPack) throws IOException, PackageTooLargeException {
        handler.getNetworkHandler().send(dataPack);
    }

    //start

    //end

    @Override
    public void onEndSucceed() {
        super.onEndSucceed();
        if (panel != null) {
            panel.onTransferSucceed(super.getFile());
        }
        if (callback != null) {
            callback.onSucceed(this);
        }
    }

    @Override
    public void onEndFailed(String reason) {
        super.onEndFailed(reason);
        if (panel != null) {
            panel.onTransferFailed(reason);
        }
        if (callback != null) {
            callback.onFailed(this, reason);
        }
    }

    //getter

    public String getFileName() {
        return fileName;
    }

    //static

    public static IDownloadCallback getDefaultCallback(FileTransferType fileTransferType) {
        switch (fileTransferType) {
            case ChatFile: {
                return new IDownloadCallback() {
                    @Override
                    public void onSucceed(ClientFileReceiveTask task) {
                        var panel = task.panel;
                        var file = task.getFile();
                        panel.onTransferSucceed(file);
                    }

                    @Override
                    public void onFailed(ClientFileReceiveTask task, String reason) {
                        var panel = task.panel;
                        panel.onTransferFailed(reason);
                    }

                };
            }
            default: {
                throw new InvalidParameterException(String.format("No default callback found for %s.", fileTransferType));
            }
        }
    }

}
