package com.omg_link.im.core.protocol.data_pack.user_list;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data_pack.DataPack;
import com.omg_link.im.core.user_manager.User;

import java.util.UUID;

public class BroadcastUserLeftPack extends DataPack {
    private final UUID uid;

    public BroadcastUserLeftPack(User user){
        super(Type.BroadcastUserLeft);
        this.uid = user.getUid();
    }

    public BroadcastUserLeftPack(ByteData data) throws InvalidPackageException {
        super(data);
        this.uid = data.decodeUuid();
    }

    @Override
    public ByteData encode() {
        return super.encode()
                .append(uid);
    }

    public UUID getUid() {
        return uid;
    }

}
