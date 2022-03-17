package im.protocol.data_pack.system;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;

public class ConnectRequestPack extends DataPack {
    private String userName;
    private String token;

    public ConnectRequestPack(String userName, String token){
        super(DataPackType.ConnectRequest);
        this.userName = userName;
        this.token = token;
    }

    public ConnectRequestPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.ConnectRequest);
        this.decode(data);
    }

    @Override
    public ByteData encode(){
        return super.encode()
                .append(ByteData.encode(userName))
                .append(ByteData.encode(token));
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        userName = data.decodeString();
        token = data.decodeString();
    }

    public String getUserName() {
        return userName;
    }

    public String getToken() {
        return token;
    }

}
