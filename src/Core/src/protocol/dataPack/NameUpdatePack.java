package protocol.dataPack;

import protocol.helper.data.ByteData;
import protocol.helper.data.InvalidPackageException;

public class NameUpdatePack extends DataPack {
    String userName;

    public NameUpdatePack(String userName){
        super(DataPackType.NameUpdate);
        this.userName = userName;
    }

    public NameUpdatePack(ByteData data) throws InvalidPackageException {
        super(DataPackType.NameUpdate);
        this.decode(data);
    }

    @Override
    public ByteData encode(){
        ByteData data = new ByteData();
        data.append(super.encode());
        data.append(new ByteData(userName));
        return data;
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        userName = ByteData.decodeString(data);
    }

    public String getUserName() {
        return userName;
    }
}
