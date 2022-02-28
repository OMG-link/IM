package protocol.dataPack;

import protocol.helper.data.ByteData;
import protocol.helper.data.InvalidPackageException;

import java.util.UUID;

public class DownloadRequestPack extends DataPack{
    private UUID receiverTaskId,fileId;
    private FileTransferType fileTransferType;

    public DownloadRequestPack(UUID receiverTaskId,UUID fileId, FileTransferType fileTransferType){
        super(DataPackType.FileDownloadRequest);
        this.receiverTaskId = receiverTaskId;
        this.fileId = fileId;
        this.fileTransferType = fileTransferType;
    }

    public DownloadRequestPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.FileDownloadRequest);
        this.decode(data);
    }

    @Override
    public ByteData encode(){
        return new ByteData()
                .append(super.encode())
                .append(ByteData.encode(receiverTaskId))
                .append(ByteData.encode(fileId))
                .append(ByteData.encode(fileTransferType.toId()));
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        receiverTaskId = data.decodeUuid();
        fileId = data.decodeUuid();
        fileTransferType = FileTransferType.toType(data.decodeInt());
    }

    public UUID getReceiverTaskId() {
        return receiverTaskId;
    }

    public UUID getFileId() {
        return fileId;
    }

    public FileTransferType getFileTransferType() {
        return fileTransferType;
    }

}
