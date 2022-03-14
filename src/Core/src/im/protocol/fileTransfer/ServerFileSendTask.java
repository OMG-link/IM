package im.protocol.fileTransfer;

import im.protocol.dataPack.DataPack;
import im.Server;
import im.file_manager.NoSuchFileIdException;
import im.file_manager.ServerFileManager;
import im.protocol.dataPack.DownloadRequestPack;
import im.protocol.dataPack.UploadRequestPack;
import im.protocol.helper.data.PackageTooLargeException;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.UUID;

public class ServerFileSendTask extends FileSendTask{
    private final Server server;
    private final SelectionKey selectionKey;

    //construct

    public ServerFileSendTask(
            Server server, SelectionKey selectionKey, UUID senderTaskId,
            DownloadRequestPack requestPack
    ) throws NoSuchFileIdException {
        super(requestPack.getFileTransferType());
        this.server = server;
        this.selectionKey = selectionKey;
        super.setSenderTaskId(senderTaskId);

        super.setReceiverTaskId(requestPack.getReceiverTaskId());
        super.setSenderFileId(requestPack.getSenderFileId());
        super.setReceiverFileId(requestPack.getReceiverFileId());
        super.setFileObject(
                getFileManager().openFile(requestPack.getSenderFileId()),
                getFileManager().getFileName(requestPack.getSenderFileId())
        );

    }

    //abstract

    @Override
    protected void send(DataPack dataPack) throws IOException, PackageTooLargeException {
        server.getNetworkHandler().send(selectionKey,dataPack);
    }

    @Override
    ServerFileManager getFileManager() {
        return server.getFileManager();
    }

    @Override
    void removeFromFactory() {
        try{
            server.getFactoryManager().getFileSendTaskFactory().remove(this);
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
