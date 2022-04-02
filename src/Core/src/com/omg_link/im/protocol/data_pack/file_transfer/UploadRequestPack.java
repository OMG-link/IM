package com.omg_link.im.protocol.data_pack.file_transfer;

import com.omg_link.im.protocol.data.ByteData;
import com.omg_link.im.protocol.file_transfer.ClientFileSendTask;
import com.omg_link.im.protocol.data_pack.DataPack;
import com.omg_link.im.protocol.file_transfer.ServerFileSendTask;
import com.omg_link.im.protocol.data.InvalidPackageException;

import java.util.UUID;

public class UploadRequestPack extends DataPack {
    private final UUID senderTaskId;
    private final UUID receiverTaskId;
    private final UUID senderFileId;
    private final UUID receiverFileId;
    private final String fileName;
    private final long fileSize;
    private final FileTransferType fileTransferType;

    /**
     * Constructor used by client.
     */
    public UploadRequestPack(ClientFileSendTask task){
        super(Type.FileUploadRequest);
        this.senderTaskId = task.getSenderTaskId();
        this.receiverTaskId = new UUID(0,0);
        this.senderFileId = task.getSenderFileId();
        this.receiverFileId = new UUID(0,0);
        this.fileName = task.getFileName();
        this.fileSize = task.getFileObject().getLength();
        this.fileTransferType = task.getFileTransferType();
    }

    /**
     * Constructor used by server.
     */
    public UploadRequestPack(ServerFileSendTask task){
        super(Type.FileUploadRequest);
        this.senderTaskId = task.getSenderTaskId();
        this.receiverTaskId = task.getReceiverTaskId();
        this.senderFileId = task.getSenderFileId();
        this.receiverFileId = task.getReceiverFileId();
        this.fileName = task.getFileName();
        this.fileSize = task.getFileObject().getLength();
        this.fileTransferType = task.getFileTransferType();
    }

    public UploadRequestPack(ByteData data) throws InvalidPackageException {
        super(data);
        assert(getType()==Type.FileUploadRequest);
        this.senderTaskId = data.decodeUuid();
        this.receiverTaskId = data.decodeUuid();
        this.senderFileId = data.decodeUuid();
        this.receiverFileId = data.decodeUuid();
        this.fileName = data.decodeString();
        this.fileSize = data.decodeLong();
        this.fileTransferType = data.decodeEnum(FileTransferType.values());
    }

    @Override
    public ByteData encode(){
        return super.encode()
                .append(senderTaskId)
                .append(receiverTaskId)
                .append(senderFileId)
                .append(receiverFileId)
                .append(fileName)
                .append(fileSize)
                .append(fileTransferType);
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
