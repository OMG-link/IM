package protocol.helper.fileTransfer;

import IM.Server;
import mutil.file.FileObject;
import mutil.uuidLocator.UUIDManager;
import protocol.dataPack.DataPack;
import protocol.dataPack.FileTransferType;
import protocol.helper.data.PackageTooLargeException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

public class ServerFileSendTask extends FileSendTask{
    private final Server handler;
    private final SocketChannel socketChannel;
    private final FileObject fileObject;
    private final String fileName;
    private final FileTransferType fileTransferType;
    private final UUID fileId;

    public ServerFileSendTask(Server handler,SocketChannel socketChannel,UUID fileId,FileTransferType fileTransferType) throws FileNotFoundException {
        this.handler = handler;
        super.init();

        this.socketChannel = socketChannel;
        this.fileObject = handler.getFileManager().getFile(fileId);
        this.fileName = handler.getFileManager().getFileName(fileId);
        this.fileTransferType = fileTransferType;
        this.fileId = fileId;

    }

    @Override
    protected UUID getFileId() {
        return fileId;
    }

    @Override
    protected String getFileName() {
        return fileName;
    }

    @Override
    protected FileTransferType getFileTransferType() {
        return fileTransferType;
    }

    @Override
    protected FileObject getFileObject() {
        return fileObject;
    }

    @Override
    protected void send(DataPack dataPack) throws IOException, PackageTooLargeException {
        handler.getNetworkHandler().send(socketChannel,dataPack);
    }

    @Override
    public UUIDManager getUuidManager() {
        return handler.getUuidManager();
    }
}
