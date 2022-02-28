package protocol.helper.fileTransfer;

import IM.Server;
import mutil.file.WriteOnlyFile;
import mutil.uuidLocator.UUIDManager;
import mutil.uuidLocator.UuidConflictException;
import protocol.dataPack.DataPack;
import protocol.dataPack.FileTransferType;
import protocol.dataPack.FileUploadedPack;
import protocol.helper.data.PackageTooLargeException;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ServerFileReceiveTask extends FileReceiveTask {
    private final Server handler;
    private final SocketChannel socketChannel;
    private final String fileName;
    private final String sender;
    private final FileTransferType fileTransferType;

    public ServerFileReceiveTask(Server handler, SocketChannel socketChannel, WriteOnlyFile file, String sender, String fileName, long fileSize, FileTransferType fileTransferType) throws UuidConflictException {
        this.handler = handler;
        this.socketChannel = socketChannel;
        super.init();

        super.setFileSize(fileSize);
        super.setWriteOnlyFile(file);
        this.fileName = fileName;
        this.sender = sender;
        this.fileTransferType = fileTransferType;
    }

    @Override
    public UUIDManager getUuidManager() {
        return handler.getUuidManager();
    }

    @Override
    protected FileTransferType getFileTransferType() {
        return fileTransferType;
    }

    @Override
    protected void send(DataPack dataPack) throws IOException, PackageTooLargeException {
        handler.getNetworkHandler().send(socketChannel, dataPack);
    }

    @Override
    protected void onEndSucceed() {
        super.onEndSucceed();
        if (fileTransferType == FileTransferType.ChatFile) {
            FileUploadedPack pack = new FileUploadedPack(sender, file.getFileObject().getFileId(), fileName, fileSize);
            handler.getNetworkHandler().broadcast(pack);
        }
    }

}
