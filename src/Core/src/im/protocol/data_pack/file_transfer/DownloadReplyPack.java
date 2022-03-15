package im.protocol.data_pack.file_transfer;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;

import java.util.UUID;

public class DownloadReplyPack extends DataPack {
    private UUID senderTaskId,receiverTaskId,senderFileId,receiverFileId;
    private boolean ok;
    private String reason;
    private FileTransferType fileTransferType;

    public DownloadReplyPack(DownloadRequestPack requestPack,UUID senderTaskId,boolean ok,String reason){
        super(DataPackType.FileDownloadReply);
        this.senderTaskId = senderTaskId;
        this.receiverTaskId = requestPack.getReceiverTaskId();
        this.senderFileId = requestPack.getSenderFileId();
        this.receiverFileId = requestPack.getReceiverFileId();
        this.ok = ok;
        this.reason = reason;
        this.fileTransferType = requestPack.getFileTransferType();
    }

    public DownloadReplyPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.FileDownloadReply);
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
                .append(ByteData.encode(ok))
                .append(ByteData.encode(reason))
                .append(ByteData.encode(fileTransferType.toId()));
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        senderTaskId = data.decodeUuid();
        receiverTaskId = data.decodeUuid();
        senderFileId = data.decodeUuid();
        receiverFileId = data.decodeUuid();
        ok = data.decodeBoolean();
        reason = data.decodeString();
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

    public boolean isOk() {
        return ok;
    }

    public String getReason() {
        return reason;
    }

    public FileTransferType getFileTransferType() {
        return fileTransferType;
    }
}
