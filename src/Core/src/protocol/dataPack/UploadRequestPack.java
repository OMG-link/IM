package protocol.dataPack;

import protocol.helper.data.ByteData;
import protocol.helper.data.InvalidPackageException;

import java.util.UUID;

public class UploadRequestPack extends DataPack {
    private UUID senderTaskId,receiverTaskId;
    private String fileName;
    private long fileSize;
    private FileTransferType fileTransferType;

    public UploadRequestPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.FileUploadRequest);
        this.decode(data);
    }

    public UploadRequestPack(String fileName, long fileSize, UUID senderTaskId, UUID receiverTaskId,FileTransferType fileTransferType){
        super(DataPackType.FileUploadRequest);
        this.senderTaskId = senderTaskId;
        this.receiverTaskId = receiverTaskId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileTransferType = fileTransferType;
    }

    @Override
    public ByteData encode(){
        ByteData data = new ByteData();
        data.append(super.encode());
        data.append(ByteData.encode(senderTaskId));
        data.append(ByteData.encode(receiverTaskId));
        data.append(new ByteData(this.fileName));
        data.append(new ByteData(this.fileSize));
        data.append(new ByteData(this.fileTransferType.toId()));
        return data;
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        this.senderTaskId = data.decodeUuid();
        this.receiverTaskId = data.decodeUuid();
        this.fileName = data.decodeString();
        this.fileSize = data.decodeLong();
        this.fileTransferType = FileTransferType.toType(data.decodeInt());
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public UUID getSenderTaskId() {
        return senderTaskId;
    }

    public UUID getReceiverTaskId() {
        return receiverTaskId;
    }

    public FileTransferType getFileTransferType() {
        return fileTransferType;
    }

}
