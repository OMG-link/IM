package protocol.helper.fileTransfer;

import GUI.IFileTransferringPanel;
import IM.Client;
import mutil.file.WriteOnlyFile;
import mutil.uuidLocator.UUIDManager;
import protocol.dataPack.DataPack;
import protocol.dataPack.FileTransferType;
import protocol.helper.data.PackageTooLargeException;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.UUID;

public class ClientFileReceiveTask extends FileReceiveTask {
    private final Client handler;
    private IFileTransferringPanel panel;
    private String fileName;
    private final FileTransferType fileTransferType;
    private final IDownloadCallback callback;

    public ClientFileReceiveTask(Client handler, FileTransferType fileTransferType, IDownloadCallback callback) {
        this.handler = handler;
        super.init();

        this.fileTransferType = fileTransferType;
        this.callback = callback;

        if(fileTransferType==FileTransferType.ChatFile){
            panel = handler.getRoomFrame().addFileTransferringPanel(this::getFileName);
        }

    }

    public ClientFileReceiveTask(Client handler, UUID uuid, FileTransferType fileTransferType) {
        this(handler,fileTransferType,getDefaultCallback(fileTransferType));
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    protected void showInfo(String info) {
        if(fileTransferType!=FileTransferType.ChatFile) return;
        panel.setInfo(info);
    }

    @Override
    public void setWriteOnlyFile(WriteOnlyFile file){
        super.setWriteOnlyFile(file);
        this.fileName = file.getFileObject().getFile().getName();
    }

    @Override
    protected void setTransferProgress(double progress){
        if(fileTransferType!=FileTransferType.ChatFile) return;
        panel.setProgress(progress);
    }

    @Override
    public UUIDManager getUuidManager() {
        return handler.getUuidManager();
    }

    @Override
    protected FileTransferType getFileTransferType() {
        return fileTransferType;
    }

    @Override
    protected void send(DataPack dataPack) throws IOException, PackageTooLargeException {
        handler.getNetworkHandler().send(dataPack);
    }

    @Override
    protected void onEndSucceed() {
        super.onEndSucceed();
        if(callback==null) return;
        callback.onSucceed(this);
    }

    @Override
    protected void onEndFailed(String reason) {
        super.onEndFailed(reason);
        if(callback==null) return;
        callback.onFailed(this);
    }

    public static IDownloadCallback getDefaultCallback(FileTransferType fileTransferType){
        switch (fileTransferType){
            case ChatFile:{
                return new IDownloadCallback() {
                    @Override
                    public void onSucceed(ClientFileReceiveTask task) {
                        var panel = task.panel;
                        var fileName = task.fileName;
                        var handler = task.handler;
                        var file = task.file;
                        panel.setInfo(String.format("Download succeed: %s",fileName));
                        handler.getGUI().onFileDownloaded(file.getFileObject().getFile());
                    }

                    @Override
                    public void onFailed(ClientFileReceiveTask task) {
                        var panel = task.panel;
                        var fileName = task.fileName;
                        panel.setInfo(String.format("Download failed: %s",fileName));
                    }

                };
            }
            default:{
                throw new InvalidParameterException(String.format("No default callback found for %s.",fileTransferType));
            }
        }
    }

}
