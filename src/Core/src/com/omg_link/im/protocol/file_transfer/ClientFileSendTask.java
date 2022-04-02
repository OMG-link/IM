package com.omg_link.im.protocol.file_transfer;

import com.omg_link.im.Client;
import com.omg_link.im.gui.IFileTransferringPanel;
import com.omg_link.im.protocol.data_pack.DataPack;
import com.omg_link.im.file_manager.FileManager;
import com.omg_link.im.protocol.data_pack.file_transfer.FileTransferType;
import com.omg_link.im.protocol.data_pack.file_transfer.UploadRequestPack;
import com.omg_link.im.protocol.data.PackageTooLargeException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.UUID;

public class ClientFileSendTask extends FileSendTask {
    private final Client client;
    private final IFileTransferringPanel panel;

    //constructors

    public ClientFileSendTask(
            Client client, UUID senderTaskId,
            File file, FileTransferType fileTransferType,
            IFileTransferringPanel panel
    ) throws FileNotFoundException {
        super(fileTransferType);
        this.client = client;
        this.panel = panel;
        super.setSenderTaskId(senderTaskId);

        super.setFile(file);

    }

    //abstract

    @Override
    protected void send(DataPack dataPack) throws PackageTooLargeException {
        this.client.getNetworkHandler().send(dataPack);
    }

    @Override
    FileManager getFileManager() {
        return client.getFileManager();
    }

    @Override
    void removeFromFactory() {
        try{
            client.getFactoryManager().getFileSendTaskFactory().remove(this);
        }catch (NoSuchTaskIdException e){
            throw new RuntimeException(e);
        }
    }

    //start

    @Override
    public void start() {
        if (panel != null) {
            panel.onTransferStart();
        }
        super.start();
    }

    @Override
    void sendUploadRequestPack() {
        try {
            send(new UploadRequestPack(this));
        } catch (PackageTooLargeException e) {
            throw new RuntimeException(e);
        }
    }

    //end

    @Override
    public void onEndSucceed() {
        if (panel != null) {
            panel.onTransferSucceed(getFileObject());
        }
        super.onEndSucceed();
    }

    @Override
    public void onEndFailed(String reason) {
        if (localEndReason != null) { //use local end reason if exists
            reason = localEndReason;
        }
        if (panel != null) {
            panel.onTransferFailed(reason);
        }
        super.onEndFailed(reason);
    }

    //functions

    @Override
    protected void onTransferProgressChange(long uploadedSize) {
        if (panel != null) {
            panel.setProgress(uploadedSize);
        }
    }

    //get

    public IFileTransferringPanel getPanel() {
        return panel;
    }

}
