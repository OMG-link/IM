package com.omg_link.im.protocol.data_pack.file_transfer;

import com.omg_link.im.protocol.data.ByteData;
import com.omg_link.im.protocol.data_pack.DataPack;
import com.omg_link.im.protocol.file_transfer.FileReceiveTask;
import com.omg_link.im.protocol.data.InvalidPackageException;

import java.util.UUID;

/**
 * Used by server to notice the end of file transfer.
 */
public class UploadResultPack extends DataPack {
    private final UUID senderTaskId, receiverTaskId;
    private final boolean ok;
    private final String reason;

    /**
     * Called by FileReceiveTask.
     */
    public UploadResultPack(FileReceiveTask task, boolean ok, String reason) {
        super(Type.FileUploadResult);
        this.senderTaskId = task.getSenderTaskId();
        this.receiverTaskId = task.getReceiverTaskId();
        this.ok = ok;
        this.reason = reason;
    }

    /**
     * Called when the task is not found.
     */
    public UploadResultPack(UUID senderTaskId){
        super(Type.FileUploadResult);
        this.senderTaskId = senderTaskId;
        this.receiverTaskId = new UUID(0,0);
        this.ok = false;
        this.reason = "Task not found.";
    }

    public UploadResultPack(ByteData data) throws InvalidPackageException {
        super(data);
        assert(getType()==Type.FileUploadResult);
        this.senderTaskId = data.decodeUuid();
        this.receiverTaskId = data.decodeUuid();
        this.ok = data.decodeBoolean();
        this.reason = data.decodeString();
    }

    @Override
    public ByteData encode() {
        return super.encode()
                .append(senderTaskId)
                .append(receiverTaskId)
                .append(ok)
                .append(reason);
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
