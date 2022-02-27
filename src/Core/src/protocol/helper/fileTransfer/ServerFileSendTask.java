package protocol.helper.fileTransfer;

import IM.Server;
import mutil.file.FileObject;
import mutil.uuidLocator.UUIDManager;
import mutil.uuidLocator.UuidConflictException;
import protocol.dataPack.DataPack;
import protocol.dataPack.UploadRequestPack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

public class ServerFileSendTask extends FileSendTask{
    private final Server handler;
    private final SocketChannel socketChannel;
    private final FileObject fileObject;
    private final String fileName;

    public ServerFileSendTask(Server handler,SocketChannel socketChannel,UUID uuid) throws FileNotFoundException {
        this.handler = handler;
        this.socketChannel = socketChannel;
        this.fileObject = handler.getFileManager().openFile(uuid);
        this.fileName = handler.getFileManager().getFileName(uuid);
    }

    @Override
    protected void sendUploadRequestPack() throws IOException {
        UploadRequestPack pack = new UploadRequestPack(
                fileName,
                getFileObject().getFile().length(),
                uuid
        );
        this.send(pack);
    }

    @Override
    protected FileObject getFileObject() {
        return fileObject;
    }

    @Override
    protected void send(DataPack dataPack) throws IOException {
        handler.getNetworkHandler().send(socketChannel,dataPack);
    }

    @Override
    public UUIDManager getUuidManager() {
        return handler.getUuidManager();
    }
}
