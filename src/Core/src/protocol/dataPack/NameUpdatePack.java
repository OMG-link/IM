package protocol.dataPack;

import protocol.helper.data.Data;
import protocol.helper.data.InvalidPackageException;

public class NameUpdatePack extends DataPack {
    String userName;

    public NameUpdatePack(String userName){
        super(DataPackType.NameUpdate);
        this.userName = userName;
    }

    public NameUpdatePack(Data data) throws InvalidPackageException {
        super(DataPackType.NameUpdate);
        this.decode(data);
    }

    @Override
    public Data encode(){
        Data data = new Data();
        data.append(super.encode());
        data.append(new Data(userName));
        return data;
    }

    @Override
    public void decode(Data data) throws InvalidPackageException {
        super.decode(data);
        userName = Data.decodeString(data);
    }

    public String getUserName() {
        return userName;
    }
}
