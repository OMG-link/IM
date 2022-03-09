package protocol.helper.fileTransfer;

import IM.Server;
import mutils.file.FileObject;
import mutils.file.FileOccupiedException;
import mutils.file.ServerFileManager;
import mutils.uuidLocator.UUIDManager;
import protocol.dataPack.DataPack;
import protocol.dataPack.FileTransferType;
import protocol.dataPack.FileUploadedPack;
import protocol.dataPack.UploadRequestPack;
import protocol.helper.data.PackageTooLargeException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ServerFileReceiveTask extends FileReceiveTask {
    private final Server handler;
    private final SocketChannel socketChannel;

    private final String sender;

    //constructor

    /**
     * @throws IOException - When the file cannot be created.
     */
    public ServerFileReceiveTask(
            Server handler, SocketChannel socketChannel,
            UploadRequestPack requestPack, String sender
    ) throws IOException {
        super(requestPack.getFileTransferType());
        this.handler = handler;
        this.socketChannel = socketChannel;
        this.sender = sender;
        super.setReceiverTaskId();

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
    public UUIDManager getUuidManager() {
        return handler.getUuidManager();
    }

    @Override
    protected ServerFileManager getFileManager() {
        return handler.getFileManager();
    }

    @Override
    protected void send(DataPack dataPack) throws IOException, PackageTooLargeException {
        handler.getNetworkHandler().send(socketChannel, dataPack);
    }

    //end

    @Override
    public void onEndSucceed() {
        super.onEndSucceed();
        if (getFileTransferType() == FileTransferType.ChatFile) {
            try{
                FileUploadedPack pack = new FileUploadedPack(sender, super.getReceiverFileId(), getFileManager().getFileName(getReceiverFileId()), super.getFileSize());
                handler.getNetworkHandler().broadcast(pack, true);
            }catch (FileNotFoundException e){
                throw new RuntimeException(e); //This never happens.
            }
        }
    }

}
