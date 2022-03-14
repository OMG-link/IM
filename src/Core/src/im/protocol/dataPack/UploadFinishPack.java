package im.protocol.dataPack;

import im.protocol.helper.data.ByteData;
import im.protocol.helper.data.InvalidPackageException;
import im.protocol.fileTransfer.FileSendTask;

import java.util.UUID;

public class UploadFinishPack extends DataPack {
    private UUID senderTaskId,receiverTaskId;

    public UploadFinishPack(FileSendTask task){
        super(DataPackType.FileUploadFinish);
        this.senderTaskId = task.getSenderTaskId();
        this.receiverTaskId = task.getReceiverTaskId();
    }

    public UploadFinishPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.FileUploadFinish);
        this.decode(data);
    }

    @Override
    public ByteData encode(){
        return new ByteData()
                .append(super.encode())
                .append(ByteData.encode(senderTaskId))
                .append(ByteData.encode(receiverTaskId));
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        senderTaskId = ByteData.decodeUuid(data);
        receiverTaskId = ByteData.decodeUuid(data);
    }

    public UUID getSenderTaskId() {
        return senderTaskId;
    }

    public UUID getReceiverTaskId() {
        return receiverTaskId;
    }

}
