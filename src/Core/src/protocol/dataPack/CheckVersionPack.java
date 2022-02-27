package protocol.dataPack;

import IM.Config;
import protocol.helper.data.Data;
import protocol.helper.data.InvalidPackageException;

public class CheckVersionPack extends DataPack {
    private String version;

    public CheckVersionPack(){
        super(DataPackType.CheckVersion);
        this.version = Config.version;
    }

    public CheckVersionPack(Data data) throws InvalidPackageException {
        super(DataPackType.CheckVersion);
        this.decode(data);
    }

    @Override
    public Data encode(){
        Data data = new Data();
        data.append(super.encode());
        data.append(Data.encodeString(version));
        return data;
    }

    @Override
    public void decode(Data data) throws InvalidPackageException {
        super.decode(data);
        this.version = Data.decodeString(data);
    }

    public String getVersion() {
        return version;
    }

}
