package protocol.fileTransfer;

import IM.Server;
import mutils.file.FileObject;
import mutils.file.FileOccupiedException;
import mutils.file.NoSuchFileIdException;
import mutils.file.ServerFileManager;
import protocol.dataPack.DataPack;
import protocol.dataPack.FileTransferType;
import protocol.dataPack.FileUploadedPack;
import protocol.dataPack.UploadRequestPack;
import protocol.helper.data.PackageTooLargeException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.UUID;

public class ServerFileReceiveTask extends FileReceiveTask {
    private final Server server;
    private final SelectionKey selectionKey;

    private final String sender;

    //constructor

    /**
     * @throws IOException - When the file cannot be created.
     */
    public ServerFileReceiveTask(
            Server server, SelectionKey selectionKey, UUID receiverTaskId,
            UploadRequestPack requestPack, String sender
    ) throws IOException {
        super(requestPack.getFileTransferType());
        this.server = server;
        this.selectionKey = selectionKey;
        this.sender = sender;
        super.setReceiverTaskId(receiverTaskId);

        super.setSenderTaskId(requestPack.getSenderTaskId());
        super.setSenderFileId(requestPack.getSenderFileId());

        try{
            FileObject fileObject = getFileManager().createFile(requestPack.getFileName());
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
        if (getFileTransferType() == FileTransferType.ChatFile) {
            try{
                FileUploadedPack pack = new FileUploadedPack(sender, super.getReceiverFileId(), getFileManager().getFileName(getReceiverFileId()), super.getFileSize());
                server.getNetworkHandler().broadcast(pack, true);
            }catch (NoSuchFileIdException e){
                throw new RuntimeException(e); //This never happens.
            }
        }
    }

}
