package protocol.helper.fileTransfer;

import IM.Server;
import mutils.file.ServerFileManager;
import mutils.uuidLocator.UUIDManager;
import protocol.dataPack.DataPack;
import protocol.dataPack.DownloadRequestPack;
import protocol.dataPack.UploadRequestPack;
import protocol.helper.data.PackageTooLargeException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ServerFileSendTask extends FileSendTask{
    private final Server handler;
    private final SocketChannel socketChannel;

    //construct

    public ServerFileSendTask(
            Server handler, SocketChannel socketChannel,
            DownloadRequestPack requestPack
    ) throws FileNotFoundException {
        super(requestPack.getFileTransferType());
        this.handler = handler;
        this.socketChannel = socketChannel;
        super.setSenderTaskId();

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
        handler.getNetworkHandler().send(socketChannel,dataPack);
    }

    @Override
    ServerFileManager getFileManager() {
        return handler.getFileManager();
    }

    @Override
    public UUIDManager getUuidManager() {
        return handler.getUuidManager();
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
