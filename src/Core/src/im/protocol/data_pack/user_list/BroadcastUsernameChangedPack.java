package im.protocol.data_pack.user_list;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;
import im.user_manager.User;

import java.util.UUID;

public class BroadcastUsernameChangedPack extends DataPack {
    private UUID uid;
    private String name;

    public BroadcastUsernameChangedPack(User user){
        super(DataPackType.BroadcastUserNameChanged);
        this.uid = user.getUid();
        this.name = user.getName();
    }

    public BroadcastUsernameChangedPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.BroadcastUserNameChanged);
        decode(data);
    }

    @Override
    public ByteData encode() {
        return super.encode()
                .append(ByteData.encode(uid))
                .append(ByteData.encode(name));
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        this.uid = data.decodeUuid();
        this.name = data.decodeString();
    }

    public UUID getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

}
