package im.protocol.data_pack.user_list;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;
import im.user_manager.User;

public class BroadcastUserJoinPack extends DataPack {
    private User user;

    public BroadcastUserJoinPack(User user){
        super(DataPackType.BroadcastUserJoin);
        this.user = user;
    }

    public BroadcastUserJoinPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.BroadcastUserJoin);
        decode(data);
    }

    @Override
    public ByteData encode() {
        return super.encode()
                .append(user.encodeToBytes());
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        this.user = new User(data);
    }

    public User getUser() {
        return user;
    }

}
