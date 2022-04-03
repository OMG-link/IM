package com.omg_link.im.core.protocol.file_transfer;

import com.omg_link.im.core.ServerRoom;
import com.omg_link.im.core.file_manager.NoSuchFileIdException;
import com.omg_link.im.core.file_manager.ServerFileManager;
import com.omg_link.im.core.protocol.data_pack.DataPack;
import com.omg_link.im.core.protocol.data_pack.file_transfer.DownloadRequestPack;
import com.omg_link.im.core.protocol.data_pack.file_transfer.UploadRequestPack;
import com.omg_link.im.core.protocol.data.PackageTooLargeException;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.UUID;

public class ServerFileSendTask extends FileSendTask{
    private final ServerRoom serverRoom;
    private final SelectionKey selectionKey;

    //construct

    public ServerFileSendTask(
            ServerRoom serverRoom, SelectionKey selectionKey, UUID senderTaskId,
            DownloadRequestPack requestPack
    ) throws NoSuchFileIdException {
        super(requestPack.getFileTransferType());
        this.serverRoom = serverRoom;
        this.selectionKey = selectionKey;
        super.setSenderTaskId(senderTaskId);

        super.setReceiverTaskId(requestPack.getReceiverTaskId());
        super.setReceiverFileId(requestPack.getReceiverFileId());
        super.setFileObject(
                getFileManager().openFile(requestPack.getSenderFileId()),
                requestPack.getSenderFileId().toString()
        );

    }

    //abstract

    @Override
    protected void send(DataPack dataPack) throws PackageTooLargeException {
        serverRoom.getNetworkHandler().send(selectionKey,dataPack);
    }

    @Override
    ServerFileManager getFileManager() {
        return serverRoom.getFileManager();
    }

    @Override
    void removeFromFactory() {
        try{
            serverRoom.getFactoryManager().getFileSendTaskFactory().remove(this);
        }catch (NoSuchTaskIdException e){
            throw new RuntimeException(e);
        }
    }

    //steps

    @Override
    void sendUploadRequestPack() throws IOException {
        try{
            send(new UploadRequestPack(this));
        }catch (PackageTooLargeException e){
            throw new RuntimeException(e);
        }
    }

}
