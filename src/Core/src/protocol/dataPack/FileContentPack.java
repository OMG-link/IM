package protocol.dataPack;

import protocol.helper.data.Data;
import protocol.helper.data.InvalidPackageException;

import java.util.Arrays;
import java.util.UUID;

public class FileContentPack extends DataPack {
    public static final int packSize = 64*1024; //64KB

    private UUID uuid;
    private long offset;
    private byte[] data;

    public FileContentPack(UUID uuid,long offset,byte[] data,int length){
        super(DataPackType.FileContent);
        this.offset = offset;
        this.uuid = uuid;
        this.data = Arrays.copyOf(data,length);
    }

    public FileContentPack(Data data) throws InvalidPackageException {
        super(DataPackType.FileContent);
        this.decode(data);
    }

    @Override
    public void decode(Data data) throws InvalidPackageException {
        super.decode(data);
        this.offset = Data.decodeLong(data);
        this.uuid = Data.decodeUuid(data);
        this.data = Data.decodeByteArray(data);
    }

    @Override
    public Data encode(){
        Data data = new Data();
        data.append(super.encode());
        data.append(new Data(offset));
        data.append(new Data(uuid));
        data.append(new Data(this.data));
        return data;
    }

    public long getOffset() {
        return offset;
    }

    public UUID getUuid() {
        return uuid;
    }

    public byte[] getData() {
        return data;
    }

}
