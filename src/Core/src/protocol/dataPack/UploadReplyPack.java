package protocol.dataPack;

import protocol.helper.data.ByteData;
import protocol.helper.data.InvalidPackageException;

import java.util.UUID;

public class UploadReplyPack extends DataPack {
    private UUID senderTaskId, receiverTaskId;
    private boolean ok;
    private String desc;
    private FileTransferType fileTransferType;

    public UploadReplyPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.FileUploadReply);
        this.decode(data);
    }

    public UploadReplyPack(UUID senderTaskId, UUID receiverTaskId, boolean ok, String desc, FileTransferType fileTransferType) {
        super(DataPackType.FileUploadReply);
        this.senderTaskId = senderTaskId;
        this.receiverTaskId = receiverTaskId;
        this.ok = ok;
        this.desc = desc;
        this.fileTransferType = fileTransferType;
    }

    @Override
    public ByteData encode() {
        return new ByteData()
                .append(super.encode())
                .append(ByteData.encode(senderTaskId))
                .append(ByteData.encode(receiverTaskId))
                .append(ByteData.encode(ok))
                .append(ByteData.encode(desc))
                .append(ByteData.encode(fileTransferType.toId()));
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        this.senderTaskId = data.decodeUuid();
        this.receiverTaskId = data.decodeUuid();
        this.ok = data.decodeBoolean();
        this.desc = data.decodeString();
        this.fileTransferType = FileTransferType.toType(data.decodeInt());
    }

    public UUID getSenderTaskId() {
        return senderTaskId;
    }

    public UUID getReceiverTaskId() {
        return receiverTaskId;
    }

    public boolean isOk() {
        return ok;
    }

    public String getDesc() {
        return desc;
    }

}
