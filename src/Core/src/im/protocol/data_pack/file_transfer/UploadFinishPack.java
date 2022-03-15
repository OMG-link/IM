package im.protocol.data_pack.file_transfer;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;
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
