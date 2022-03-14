package protocol.dataPack;

import protocol.helper.data.ByteData;
import protocol.helper.data.InvalidPackageException;
import protocol.fileTransfer.FileReceiveTask;

import java.util.UUID;

/**
 * Used by server to notice the end of file transfer.
 */
public class UploadResultPack extends DataPack {
    private UUID senderTaskId, receiverTaskId;
    private boolean ok;
    private String reason;

    /**
     * Called by FileReceiveTask.
     */
    public UploadResultPack(FileReceiveTask task, boolean ok, String reason) {
        super(DataPackType.FileUploadResult);
        this.senderTaskId = task.getSenderTaskId();
        this.receiverTaskId = task.getReceiverTaskId();
        this.ok = ok;
        this.reason = reason;
    }

    /**
     * Called when the task is not found.
     */
    public UploadResultPack(UUID senderTaskId){
        super(DataPackType.FileUploadResult);
        this.senderTaskId = senderTaskId;
        this.receiverTaskId = new UUID(0,0);
        this.ok = false;
        this.reason = "Task not found.";
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
                .append(ByteData.encode(receiverTaskId))
                .append(ByteData.encode(ok))
                .append(ByteData.encode(reason));
        return data;
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        this.senderTaskId = data.decodeUuid();
        this.receiverTaskId = data.decodeUuid();
        this.ok = data.decodeBoolean();
        this.reason = data.decodeString();
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

    public String getReason() {
        return reason;
    }

}
