package com.omg_link.im.protocol.file_transfer;

import com.omg_link.im.Server;
import com.omg_link.im.file_manager.FileObject;
import com.omg_link.im.file_manager.FileOccupiedException;
import com.omg_link.im.file_manager.ServerFileManager;
import com.omg_link.im.protocol.Attachment;
import com.omg_link.im.protocol.data.PackageTooLargeException;
import com.omg_link.im.protocol.data_pack.DataPack;
import com.omg_link.im.protocol.data_pack.file_transfer.UploadRequestPack;
import com.omg_link.im.user_manager.User;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.UUID;

public class ServerFileReceiveTask extends FileReceiveTask {
    private final Server server;
    private final SelectionKey selectionKey;
    private final String fileName;

    //constructor

    /**
     * @throws IOException - When the file cannot be created.
     */
    public ServerFileReceiveTask(
            Server server, SelectionKey selectionKey, UUID receiverTaskId,
            UploadRequestPack requestPack
    ) throws IOException {
        super(requestPack.getFileTransferType());
        this.server = server;
        this.selectionKey = selectionKey;
        this.fileName = requestPack.getFileName();
        super.setReceiverTaskId(receiverTaskId);

        super.setSenderTaskId(requestPack.getSenderTaskId());
        super.setSenderFileId(requestPack.getSenderFileId());

        try{
            FileObject fileObject = getFileManager().createFile();
            fileObject.setLength(requestPack.getFileSize());
            super.setReceiverFileId(fileObject.getFileId());
            super.setFileWriter(fileObject.getWriteOnlyInstance());
        }catch (FileNotFoundException| FileOccupiedException e){
            throw new RuntimeException(e); //Never happens.
        }

    }

    //abstract

    @Override
    protected ServerFileManager getFileManager() {
        return server.getFileManager();
    }

    @Override
    void removeFromFactory() {
        try{
            server.getFactoryManager().getFileReceiveTaskFactory().remove(this);
        }catch (NoSuchTaskIdException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void send(DataPack dataPack) throws IOException, PackageTooLargeException {
        server.getNetworkHandler().send(selectionKey, dataPack);
    }

    //end

    @Override
    public void onEndSucceed() {
        super.onEndSucceed();
        User user = ((Attachment)selectionKey.attachment()).user;
        switch (getFileTransferType()){
            case ChatFile:{
                UUID fileId = getReceiverFileId();
                server.getMessageManager().broadcastChatFile(
                        user,
                        fileId,
                        fileName,
                        getFileSize()
                );
                break;
            }
            case ChatImage:{
                server.getMessageManager().broadcastChatImage(
                        user,
                        getReceiverFileId()
                );
                break;
            }
        }
    }

}
