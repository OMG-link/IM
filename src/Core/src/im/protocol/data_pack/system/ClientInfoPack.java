package im.protocol.data_pack.system;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;

public class ClientInfoPack extends DataPack {
    private String userName;

    public ClientInfoPack(String userName){
        super(DataPackType.ClientInfo);
        this.userName = userName;
    }

    public ClientInfoPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.ClientInfo);
        this.decode(data);
    }

    @Override
    public ByteData encode(){
        return super.encode()
                .append(ByteData.encode(userName));
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        userName = data.decodeString();
    }

    public String getUserName() {
        return userName;
    }
}
