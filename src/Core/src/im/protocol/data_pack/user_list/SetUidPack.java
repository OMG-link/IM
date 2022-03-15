package im.protocol.data_pack.user_list;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;
import im.user_manager.User;

import java.util.UUID;

/**
 * Server -> Client
 * <p>
 * Change UID.
 */
public class SetUidPack extends DataPack {
    private UUID uid;

    public SetUidPack(User user){
        super(DataPackType.SetUid);
        this.uid = user.getUid();
    }

    public SetUidPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.SetUid);
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
        uid = data.decodeUuid();
    }

    public UUID getUid() {
        return uid;
    }

}
