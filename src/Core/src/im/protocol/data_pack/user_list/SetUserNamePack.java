package im.protocol.data_pack.user_list;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;

import java.util.UUID;

public class SetUserNamePack extends DataPack {
    private UUID uid;
    private String userName;

    public SetUserNamePack(String userName){
        super(DataPackType.SetUserName);
        this.uid = new UUID(0,0);
        this.userName = userName;
    }

    public SetUserNamePack(ByteData data) throws InvalidPackageException {
        super(DataPackType.SetUserName);
        this.decode(data);
    }

    @Override
    public ByteData encode(){
        return super.encode()
                .append(ByteData.encode(uid))
                .append(ByteData.encode(userName));
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        uid = data.decodeUuid();
        userName = data.decodeString();
    }

    public String getUserName() {
        return userName;
    }
}
