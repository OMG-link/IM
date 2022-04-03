package com.omg_link.im.core.protocol.data_pack.file_transfer;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data_pack.DataPack;

import java.util.UUID;

public class UploadReplyPack extends DataPack {

    public enum Reason{
        ok, fileTooLarge, taskNotFound, remoteIOError
    }

    private final UUID senderTaskId;
    private final UUID receiverTaskId;
    private final UUID senderFileId;
    private final UUID receiverFileId;
    private final boolean ok;
    private final Reason reason;
    private final FileTransferType fileTransferType;

    /**
     * Constructor used by client.
     */
    public UploadReplyPack(UploadRequestPack requestPack,boolean ok,Reason reason){
        super(Type.FileUploadReply);
        this.senderTaskId = requestPack.getSenderTaskId();
        this.receiverTaskId = requestPack.getReceiverTaskId();
        this.senderFileId = requestPack.getSenderFileId();
        this.receiverFileId = requestPack.getReceiverFileId();
        this.ok = ok;
        this.reason = reason;
        this.fileTransferType = requestPack.getFileTransferType();
    }

    /**
     * Constructor used by server.
     */
    public UploadReplyPack(UploadRequestPack requestPack,UUID receiverTaskId,UUID receiverFileId,boolean ok,Reason reason){
        super(Type.FileUploadReply);
        this.senderTaskId = requestPack.getSenderTaskId();
        this.receiverTaskId = receiverTaskId;
        this.senderFileId = requestPack.getSenderFileId();
        this.receiverFileId = receiverFileId;
        this.ok = ok;
        this.reason = reason;
        this.fileTransferType = requestPack.getFileTransferType();
    }

    public UploadReplyPack(ByteData data) throws InvalidPackageException {
        super(data);
        assert(getType()==Type.FileUploadReply);
        this.senderTaskId = data.decodeUuid();
        this.receiverTaskId = data.decodeUuid();
        this.senderFileId = data.decodeUuid();
        this.receiverFileId = data.decodeUuid();
        this.ok = data.decodeBoolean();
        this.reason = data.decodeEnum(Reason.values());
        this.fileTransferType = data.decodeEnum(FileTransferType.values());
    }

    @Override
    public ByteData encode() {
        return super.encode()
                .append(senderTaskId)
                .append(receiverTaskId)
                .append(senderFileId)
                .append(receiverFileId)
                .append(ok)
                .append(reason)
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

    public boolean isOk() {
        return ok;
    }

    public Reason getReason() {
        return reason;
    }

    public FileTransferType getFileTransferType() {
        return fileTransferType;
    }
}
