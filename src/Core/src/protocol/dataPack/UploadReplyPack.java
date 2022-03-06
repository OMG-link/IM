package protocol.dataPack;

import protocol.helper.data.ByteData;
import protocol.helper.data.InvalidPackageException;

import java.util.UUID;

public class UploadReplyPack extends DataPack {
    private UUID senderTaskId;
    private UUID receiverTaskId;
    private UUID senderFileId;
    private UUID receiverFileId;
    private boolean ok;
    private String reason;
    private FileTransferType fileTransferType;

    /**
     * Constructor used by client.
     */
    public UploadReplyPack(UploadRequestPack requestPack,boolean ok,String reason){
        super(DataPackType.FileUploadReply);
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
    public UploadReplyPack(UploadRequestPack requestPack,UUID receiverTaskId,UUID receiverFileId,boolean ok,String reason){
        super(DataPackType.FileUploadReply);
        this.senderTaskId = requestPack.getSenderTaskId();
        this.receiverTaskId = receiverTaskId;
        this.senderFileId = requestPack.getSenderFileId();
        this.receiverFileId = receiverFileId;
        this.ok = ok;
        this.reason = reason;
        this.fileTransferType = requestPack.getFileTransferType();
    }

    public UploadReplyPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.FileUploadReply);
        this.decode(data);
    }

    @Override
    public ByteData encode() {
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
