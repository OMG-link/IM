package com.omg_link.im.core.protocol.file_transfer;

import com.omg_link.im.core.ClientRoom;
import com.omg_link.im.core.file_manager.ClientFileManager;
import com.omg_link.im.core.file_manager.FileObject;
import com.omg_link.im.core.gui.IFileTransferringPanel;
import com.omg_link.im.core.protocol.data.PackageTooLargeException;
import com.omg_link.im.core.protocol.data_pack.DataPack;
import com.omg_link.im.core.protocol.data_pack.file_transfer.FileTransferType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class ClientFileReceiveTask extends FileReceiveTask {
    private final ClientRoom room;
    private final Collection<IFileTransferringPanel> panels = new ArrayList<>();

    //constructors

    /**
     * @throws IOException When the file cannot be created.
     */
    public ClientFileReceiveTask(
            ClientRoom room, UUID receiverTaskId,
            String fileName, UUID senderFileId, FileTransferType fileTransferType,
            IFileTransferringPanel panel
    ) throws IOException {
        super(fileName, fileTransferType, receiverTaskId);
        this.room = room;
        super.setSenderFileId(senderFileId);

        FileObject fileObject;
        if(fileTransferType==FileTransferType.ChatFile){
            fileObject = getFileManager().createFileRenameable(ClientFileManager.downloadFolder,fileName);
        }else{
            fileObject = getFileManager().createCacheFile(ClientFileManager.downloadFolder);
        }
        super.setReceiverFileId(fileObject.getFileId());
        super.setFileWriter(fileObject.getWriteOnlyInstance());

        addPanel(panel);

        if(fileTransferType==FileTransferType.ChatImage){
            room.getFileManager().addDownloadingFile(senderFileId,receiverTaskId);
        }

    }

    public void addPanel(IFileTransferringPanel panel){
        if(panel!=null){
            panels.add(panel);
        }
    }

    //abstract

    @Override
    protected void onTransferProgressChange(long downloadedSize) {
        for(var panel:panels){
            panel.setProgress(downloadedSize);
        }
    }

    @Override
    protected ClientFileManager getFileManager() {
        return room.getFileManager();
    }

    @Override
    void removeFromFactory() {
        try{
            room.getFactoryManager().getFileReceiveTaskFactory().remove(this);
        }catch (NoSuchTaskIdException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void send(DataPack dataPack) throws PackageTooLargeException {
        room.getNetworkHandler().send(dataPack);
    }

    //end


    @Override
    public void end() {
        super.end();
        if(getFileTransferType()==FileTransferType.ChatImage){
            getFileManager().removeDownloadingFile(senderFileId);
        }
    }

    @Override
    public void onEndSucceed() {
        super.onEndSucceed();
        if (getFileTransferType()==FileTransferType.ChatImage){
            getFileManager().addMapping(senderFileId,super.getFileObject().getFile());
        }
        for(var panel:panels) {
            panel.onTransferSucceed(super.getFileObject());
        }
    }

    @Override
    public void onEndFailed(String reason) {
        super.onEndFailed(reason);
        for(var panel:panels) {
            panel.onTransferFailed(reason);
        }
    }

}
