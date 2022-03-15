package im.protocol.data_pack.file_transfer;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;

import java.util.Arrays;
import java.util.UUID;

public class FileContentPack extends DataPack {
    public static final int packSize = 63*1024; //63KB (1kb left for other information)

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
        return new ByteData()
                .append(super.encode())
                .append(ByteData.encode(offset))
                .append(ByteData.encode(receiverTaskId))
                .append(ByteData.encode(this.data));
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
