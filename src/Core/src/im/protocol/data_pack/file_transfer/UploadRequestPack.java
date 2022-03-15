package im.protocol.data_pack.file_transfer;

import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;
import im.protocol.fileTransfer.ClientFileSendTask;
import im.protocol.fileTransfer.ServerFileSendTask;
import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;

import java.util.UUID;

public class UploadRequestPack extends DataPack {
    private UUID senderTaskId;
    private UUID receiverTaskId;
    private UUID senderFileId;
    private UUID receiverFileId;
    private String fileName;
    private long fileSize;
    private FileTransferType fileTransferType;

    /**
     * Constructor used by client.
     */
    public UploadRequestPack(ClientFileSendTask task){
        super(DataPackType.FileUploadRequest);
        this.senderTaskId = task.getSenderTaskId();
        this.receiverTaskId = new UUID(0,0);
        this.senderFileId = task.getSenderFileId();
        this.receiverFileId = new UUID(0,0);
        this.fileName = task.getFileName();
        this.fileSize = task.getFile().length();
        this.fileTransferType = task.getFileTransferType();
    }

    /**
     * Constructor used by server.
     */
    public UploadRequestPack(ServerFileSendTask task){
        super(DataPackType.FileUploadRequest);
        this.senderTaskId = task.getSenderTaskId();
        this.receiverTaskId = task.getReceiverTaskId();
        this.senderFileId = task.getSenderFileId();
        this.receiverFileId = task.getReceiverFileId();
        this.fileName = task.getFileName();
        this.fileSize = task.getFile().length();
        this.fileTransferType = task.getFileTransferType();
    }

    public UploadRequestPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.FileUploadRequest);
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
                .append(ByteData.encode(fileName))
                .append(ByteData.encode(fileSize))
                .append(ByteData.encode(fileTransferType.toId()));
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        senderTaskId = data.decodeUuid();
        receiverTaskId = data.decodeUuid();
        senderFileId = data.decodeUuid();
        receiverFileId = data.decodeUuid();
        fileName = data.decodeString();
        fileSize = data.decodeLong();
        fileTransferType = FileTransferType.toType(data.decodeInt());
    }


    public UUID getSenderTaskId() {
        return senderTaskId;
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

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public FileTransferType getFileTransferType() {
        return fileTransferType;
    }

}
