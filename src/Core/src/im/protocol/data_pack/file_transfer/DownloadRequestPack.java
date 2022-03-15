package im.protocol.data_pack.file_transfer;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;

import java.util.UUID;

public class DownloadRequestPack extends DataPack {
    private UUID senderTaskId,receiverTaskId,senderFileId,receiverFileId;
    private FileTransferType fileTransferType;

    public DownloadRequestPack(UUID receiverTaskId,UUID senderFileId, UUID receiverFileId, FileTransferType fileTransferType){
        super(DataPackType.FileDownloadRequest);
        this.senderTaskId = new UUID(0,0);
        this.receiverTaskId = receiverTaskId;
        this.senderFileId = senderFileId;
        this.receiverFileId = receiverFileId;
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
                .append(ByteData.encode(senderTaskId))
                .append(ByteData.encode(receiverTaskId))
                .append(ByteData.encode(senderFileId))
                .append(ByteData.encode(receiverFileId))
                .append(ByteData.encode(fileTransferType.toId()));
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        senderTaskId = data.decodeUuid();
        receiverTaskId = data.decodeUuid();
        senderFileId = data.decodeUuid();
        receiverFileId = data.decodeUuid();
        fileTransferType = FileTransferType.toType(data.decodeInt());
    }

    public UUID getReceiverTaskId() {
        return receiverTaskId;
    }

    public UUID getSenderFileId() {
        return senderFileId;
    }

    public UUID getReceiverFileId() {
        return receiverFileId;
    }

    public FileTransferType getFileTransferType() {
        return fileTransferType;
    }

}
