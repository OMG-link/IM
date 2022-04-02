package com.omg_link.im.protocol.file_transfer;

import com.omg_link.im.Client;
import com.omg_link.im.file_manager.ClientFileManager;
import com.omg_link.im.file_manager.FileObject;
import com.omg_link.im.gui.IFileTransferringPanel;
import com.omg_link.im.protocol.data.PackageTooLargeException;
import com.omg_link.im.protocol.data_pack.DataPack;
import com.omg_link.im.protocol.data_pack.file_transfer.FileTransferType;

import java.io.IOException;
import java.util.UUID;

public class ClientFileReceiveTask extends FileReceiveTask {
    private final Client client;
    private final IFileTransferringPanel panel;
    private final String fileName;

    //constructors

    /**
     * @throws IOException When the file cannot be created.
     */
    public ClientFileReceiveTask(
            Client client, UUID receiverTaskId,
            String fileName, UUID senderFileId, FileTransferType fileTransferType,
            IFileTransferringPanel panel
    ) throws IOException {
        super(fileTransferType);
        this.client = client;
        this.fileName = fileName;
        this.panel = panel;
        super.setReceiverTaskId(receiverTaskId);
        super.setSenderFileId(senderFileId);

        FileObject fileObject;
        if(fileTransferType==FileTransferType.ChatFile){
            fileObject = getFileManager().createFileRenameable(ClientFileManager.downloadFolder,fileName);
        }else{
            fileObject = getFileManager().createCacheFile();
        }
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
    protected ClientFileManager getFileManager() {
        return client.getFileManager();
    }

    @Override
    void removeFromFactory() {
        try{
            client.getFactoryManager().getFileReceiveTaskFactory().remove(this);
        }catch (NoSuchTaskIdException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void send(DataPack dataPack) throws PackageTooLargeException {
        client.getNetworkHandler().send(dataPack);
    }

    //start

    //end

    @Override
    public void onEndSucceed() {
        super.onEndSucceed();
        if (panel != null) {
            panel.onTransferSucceed(super.getFileObject());
        }
    }

    @Override
    public void onEndFailed(String reason) {
        super.onEndFailed(reason);
        if (panel != null) {
            panel.onTransferFailed(reason);
        }
    }

    //getter

    public String getFileName() {
        return fileName;
    }

}
