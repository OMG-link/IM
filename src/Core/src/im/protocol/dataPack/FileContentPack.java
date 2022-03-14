package im.protocol.dataPack;

import im.protocol.helper.data.ByteData;
import im.protocol.helper.data.InvalidPackageException;

import java.util.Arrays;
import java.util.UUID;

public class FileContentPack extends DataPack {
    public static final int packSize = 64*1024; //64KB

    private UUID receiverTaskId;
    private long offset;
    private byte[] data;

    public FileContentPack(UUID receiverTaskId,long offset,byte[] data,int length){
        super(DataPackType.FileContent);
        this.offset = offset;
        this.receiverTaskId = receiverTaskId;
        this.data = Arrays.copyOf(data,length);
    }

    public FileContentPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.FileContent);
        this.decode(data);
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        this.offset = ByteData.decodeLong(data);
        this.receiverTaskId = ByteData.decodeUuid(data);
        this.data = ByteData.decodeByteArray(data);
    }

    @Override
    public ByteData encode(){
        ByteData data = new ByteData();
        data.append(super.encode());
        data.append(new ByteData(offset));
        data.append(new ByteData(receiverTaskId));
        data.append(new ByteData(this.data));
        return data;
    }

    public long getOffset() {
        return offset;
    }

    public UUID getReceiverTaskId() {
        return receiverTaskId;
    }

    public byte[] getData() {
        return data;
    }

}
