package im.protocol.data_pack.user_list;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;
import im.user_manager.User;

import java.util.UUID;

public class BroadcastUserLeftPack extends DataPack {
    private UUID uid;

    public BroadcastUserLeftPack(User user){
        super(DataPackType.BroadcastUserLeft);
        this.uid = user.getUid();
    }

    public BroadcastUserLeftPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.BroadcastUserLeft);
        decode(data);
    }

    @Override
    public ByteData encode() {
        return super.encode()
                .append(ByteData.encode(uid));
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        this.uid = data.decodeUuid();
    }

    public UUID getUid() {
        return uid;
    }

}
