package com.omg_link.im.core.protocol.data_pack.user_list;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data_pack.DataPack;
import com.omg_link.im.core.user_manager.User;

import java.util.UUID;

public class BroadcastUsernameChangedPack extends DataPack {
    private final UUID uid;
    private final String name;

    public BroadcastUsernameChangedPack(User user){
        super(Type.BroadcastUserNameChanged);
        this.uid = user.getUid();
        this.name = user.getName();
    }

    public BroadcastUsernameChangedPack(ByteData data) throws InvalidPackageException {
        super(data);
        this.uid = data.decodeUuid();
        this.name = data.decodeString();
    }

    @Override
    public ByteData encode() {
        return super.encode()
                .append(uid)
                .append(name);
    }

    public UUID getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

}
