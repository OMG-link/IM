package com.omg_link.im.protocol.data_pack.user_list;

import com.omg_link.im.config.Config;
import com.omg_link.im.protocol.data.ByteData;
import com.omg_link.im.protocol.data.InvalidPackageException;
import com.omg_link.im.protocol.data_pack.DataPack;

public class SetRoomNamePack extends DataPack {
    private final String roomName;

    public SetRoomNamePack(){
        super(Type.SetRoomName);
        roomName = Config.getRoomName();
    }

    public SetRoomNamePack(ByteData data) throws InvalidPackageException {
        super(data);
        this.roomName = data.decodeString();
    }

    @Override
    public ByteData encode(){
        return super.encode()
                .append(roomName);
    }

    public String getRoomName() {
        return roomName;
    }

}
