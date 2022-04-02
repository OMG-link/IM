package com.omg_link.im.protocol.data_pack.file_transfer;

import com.omg_link.im.protocol.data.ByteData;
import com.omg_link.im.protocol.data_pack.DataPack;
import com.omg_link.im.protocol.file_transfer.FileSendTask;
import com.omg_link.im.protocol.data.InvalidPackageException;

import java.util.UUID;

public class UploadFinishPack extends DataPack {
    private final UUID senderTaskId,receiverTaskId;

    public UploadFinishPack(FileSendTask task){
        super(Type.FileUploadFinish);
        this.senderTaskId = task.getSenderTaskId();
        this.receiverTaskId = task.getReceiverTaskId();
    }

    public UploadFinishPack(ByteData data) throws InvalidPackageException {
        super(data);
        assert(getType()==Type.FileUploadFinish);
        this.senderTaskId = data.decodeUuid();
        this.receiverTaskId = data.decodeUuid();
    }

    @Override
    public ByteData encode(){
        return super.encode()
                .append(senderTaskId)
                .append(receiverTaskId);
    }

    public UUID getSenderTaskId() {
        return senderTaskId;
    }

    public UUID getReceiverTaskId() {
        return receiverTaskId;
    }

}
