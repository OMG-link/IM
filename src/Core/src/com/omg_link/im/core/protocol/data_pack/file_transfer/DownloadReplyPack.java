package com.omg_link.im.core.protocol.data_pack.file_transfer;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data_pack.DataPack;

import java.util.UUID;

public class DownloadReplyPack extends DataPack {

    public enum Reason{
        ok, fileNotFound
    }

    private final UUID senderTaskId,receiverTaskId,senderFileId,receiverFileId;
    private final boolean ok;
    private final Reason reason;
    private final FileTransferType fileTransferType;

    public DownloadReplyPack(DownloadRequestPack requestPack,UUID senderTaskId,boolean ok,Reason reason){
        super(Type.FileDownloadReply);
        this.senderTaskId = senderTaskId;
        this.receiverTaskId = requestPack.getReceiverTaskId();
        this.senderFileId = requestPack.getSenderFileId();
        this.receiverFileId = requestPack.getReceiverFileId();
        this.ok = ok;
        this.reason = reason;
        this.fileTransferType = requestPack.getFileTransferType();
    }

    public DownloadReplyPack(ByteData data) throws InvalidPackageException {
        super(data);
        assert(getType()==Type.FileDownloadReply);
        this.senderTaskId = data.decodeUuid();
        this.receiverTaskId = data.decodeUuid();
        this.senderFileId = data.decodeUuid();
        this.receiverFileId = data.decodeUuid();
        this.ok = data.decodeBoolean();
        this.reason = data.decodeEnum(Reason.values());
        this.fileTransferType = data.decodeEnum(FileTransferType.values());
    }

    @Override
    public ByteData encode(){
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
