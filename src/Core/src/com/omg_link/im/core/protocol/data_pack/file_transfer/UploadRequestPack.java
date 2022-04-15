package com.omg_link.im.core.protocol.data_pack.file_transfer;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data_pack.DataPack;
import com.omg_link.im.core.protocol.file_transfer.ClientFileSendTask;
import com.omg_link.im.core.protocol.file_transfer.FileTransferType;
import com.omg_link.im.core.protocol.file_transfer.ServerFileSendTask;
import com.omg_link.utils.Sha512Digest;

import java.io.IOException;
import java.util.UUID;

public class UploadRequestPack extends DataPack {
    private final UUID senderTaskId;
    private final UUID receiverTaskId;
    private final UUID senderFileId;
    private final UUID receiverFileId;
    private final String fileName;
    private final long fileSize;
    private final FileTransferType fileTransferType;
    private final Sha512Digest sha512Digest;

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
        try{
            this.sha512Digest = task.getFileObject().getSha512Digest();
        }catch (IOException e){
            throw new RuntimeException("An I/O error occurs when calculating the digest.",e);
        }
    }

    /**
     * Constructor used by server.
     */
    public UploadRequestPack(ServerFileSendTask task,Sha512Digest digest){
        super(Type.FileUploadRequest);
        this.senderTaskId = task.getSenderTaskId();
        this.receiverTaskId = task.getReceiverTaskId();
        this.senderFileId = task.getSenderFileId();
        this.receiverFileId = task.getReceiverFileId();
        this.fileName = task.getFileName();
        this.fileSize = task.getFileObject().getLength();
        this.fileTransferType = task.getFileTransferType();
        this.sha512Digest = digest;
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
        this.sha512Digest = new Sha512Digest(data);
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
                .append(fileTransferType)
                .append(sha512Digest);
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

    public Sha512Digest getSha512Digest() {
        return sha512Digest;
    }

}
