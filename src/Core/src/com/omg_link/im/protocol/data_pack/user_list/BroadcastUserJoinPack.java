package com.omg_link.im.protocol.data_pack.user_list;

import com.omg_link.im.protocol.data.ByteData;
import com.omg_link.im.protocol.data.InvalidPackageException;
import com.omg_link.im.protocol.data_pack.DataPack;
import com.omg_link.im.user_manager.User;

public class BroadcastUserJoinPack extends DataPack {
    private final User user;

    public BroadcastUserJoinPack(User user){
        super(Type.BroadcastUserJoin);
        this.user = user;
    }

    public BroadcastUserJoinPack(ByteData data) throws InvalidPackageException {
        super(data);
        this.user = new User(data);
    }

    @Override
    public ByteData encode() {
        return super.encode()
                .append(user);
    }

    public User getUser() {
        return user;
    }

}
