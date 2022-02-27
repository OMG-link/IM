package protocol.dataPack;

import protocol.helper.data.Data;
import protocol.helper.data.InvalidPackageException;

import java.util.UUID;

public class DownloadRequestPack extends DataPack{
    private UUID uuid;

    public DownloadRequestPack(UUID uuid){
        super(DataPackType.FileDownloadRequest);
        this.uuid = uuid;
    }

    public DownloadRequestPack(Data data) throws InvalidPackageException {
        super(DataPackType.FileDownloadRequest);
        this.decode(data);
    }

    @Override
    public Data encode(){
        Data data = new Data();
        data.append(super.encode());
        data.append(new Data(uuid));
        return data;
    }

    @Override
    public void decode(Data data) throws InvalidPackageException {
        super.decode(data);
        uuid = Data.decodeUuid(data);
    }

    public UUID getUuid() {
        return uuid;
    }
}
