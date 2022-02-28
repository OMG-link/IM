package protocol.helper.fileTransfer;

import GUI.IFileTransferringPanel;
import IM.Client;
import mutil.file.FileObject;
import mutil.uuidLocator.UUIDManager;
import protocol.dataPack.DataPack;
import protocol.dataPack.FileTransferType;
import protocol.helper.data.PackageTooLargeException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.UUID;

public class ClientFileSendTask extends FileSendTask {
    private final Client handler;
    private final IFileTransferringPanel panel;
    private final FileObject fileObject;
    private final String fileName;
    private final FileTransferType fileTransferType;
    private final IUploadCallback callback;

    private UUID uploadedFileId;

    public ClientFileSendTask(Client handler, File file, FileTransferType fileTransferType, IUploadCallback callback) throws FileNotFoundException {
        this.handler = handler;
        super.init();

        this.panel = handler.getRoomFrame().addFileTransferringPanel(file::getName);
        this.fileName = file.getName();
        this.fileObject = handler.getFileManager().openFile(file);
        this.fileTransferType = fileTransferType;
        this.callback = callback;
    }

    public UUID getUploadedFileId() {
        if(uploadedFileId==null){
            throw new RuntimeException("Uploaded file ID not set.");
        }
        return uploadedFileId;
    }

    public void setUploadedFileId(UUID uploadedFileId) {
        this.uploadedFileId = uploadedFileId;
    }

    public IFileTransferringPanel getPanel() {
        return panel;
    }

    @Override
    protected void end() {
        getPanel().setVisible(false);
        super.end();
    }

    @Override
    public void onEndSucceed() {
        super.onEndSucceed();
        if (callback == null) return;
        callback.onSucceed(this);
    }

    @Override
    public void onEndFailed(String reason) {
        super.onEndFailed(reason);
        if (callback == null) return;
        callback.onFailed(this,reason);
    }

    @Override
    public UUID getFileId() {
        return fileObject.getFileId();
    }

    @Override
    protected String getFileName() {
        return fileName;
    }

    @Override
    protected FileTransferType getFileTransferType() {
        return fileTransferType;
    }

    @Override
    public FileObject getFileObject() {
        return fileObject;
    }

    @Override
    protected void send(DataPack dataPack) {
        try {
            this.handler.getNetworkHandler().send(dataPack);
        } catch (PackageTooLargeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void showInfo(String info) {
        panel.setInfo("[" + fileName + "] " + info);
    }

    @Override
    protected void setUploadProgress(double progress) {
        panel.setProgress(progress);
    }

    @Override
    public UUIDManager getUuidManager() {
        return handler.getUuidManager();
    }

}
