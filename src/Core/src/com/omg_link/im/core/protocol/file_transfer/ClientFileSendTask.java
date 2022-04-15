package com.omg_link.im.core.protocol.file_transfer;

import com.omg_link.im.core.ClientRoom;
import com.omg_link.im.core.file_manager.FileManager;
import com.omg_link.im.core.gui.IFileTransferringPanel;
import com.omg_link.im.core.protocol.data.PackageTooLargeException;
import com.omg_link.im.core.protocol.data_pack.DataPack;
import com.omg_link.im.core.protocol.data_pack.file_transfer.UploadRequestPack;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.UUID;

public class ClientFileSendTask extends FileSendTask {
    private final ClientRoom room;
    private final IFileTransferringPanel panel;

    //constructors

    public ClientFileSendTask(
            ClientRoom room, UUID senderTaskId,
            File file, FileTransferType fileTransferType,
            IFileTransferringPanel panel
    ) throws FileNotFoundException {
        super(fileTransferType);
        this.room = room;
        this.panel = panel;
        super.setSenderTaskId(senderTaskId);

        super.setFile(file);

    }

    //abstract

    @Override
    protected void send(DataPack dataPack) throws PackageTooLargeException {
        this.room.getNetworkHandler().send(dataPack);
    }

    @Override
    FileManager getFileManager() {
        return room.getFileManager();
    }

    @Override
    void removeFromFactory() {
        try{
            room.getFactoryManager().getFileSendTaskFactory().remove(this);
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
    protected void onEndSucceed() {
        if (panel != null) {
            panel.onTransferSucceed(getFileObject());
        }
        super.onEndSucceed();
    }

    @Override
    protected void onEndFailed(String state) {
        if (localEndReason != null) { //use local end state if exists
            state = localEndReason;
        }
        if (panel != null) {
            panel.onTransferFailed(state);
        }
        super.onEndFailed(state);
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
