package protocol.dataPack;

import protocol.helper.data.ByteData;
import protocol.helper.data.InvalidPackageException;

import java.util.UUID;

/**
 * Used by server to notice the end of file transfer.
 */
public class UploadResultPack extends DataPack {
    private UUID senderTaskId, uploadedFileId;
    private boolean ok;
    private String reason;

    public UploadResultPack(UUID senderTaskId, UUID uploadedFileId, boolean ok, String reason) {
        super(DataPackType.FileUploadResult);
        this.senderTaskId = senderTaskId;
        this.uploadedFileId = uploadedFileId;
        this.ok = ok;
        this.reason = reason;
    }

    public UploadResultPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.FileUploadResult);
        this.decode(data);
    }

    @Override
    public ByteData encode() {
        ByteData data = new ByteData();
        data.append(super.encode())
                .append(ByteData.encode(senderTaskId))
                .append(ByteData.encode(uploadedFileId))
                .append(ByteData.encode(ok))
                .append(ByteData.encode(reason));
        return data;
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        this.senderTaskId = data.decodeUuid();
        this.uploadedFileId = data.decodeUuid();
        this.ok = data.decodeBoolean();
        this.reason = data.decodeString();
    }

    public UUID getSenderTaskId() {
        return senderTaskId;
    }

    public UUID getUploadedFileId() {
        return uploadedFileId;
    }

    public boolean isOk() {
        return ok;
    }

    public String getReason() {
        return reason;
    }

}
