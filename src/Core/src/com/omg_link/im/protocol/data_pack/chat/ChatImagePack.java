package com.omg_link.im.protocol.data_pack.chat;

import com.omg_link.im.protocol.data.ByteData;
import com.omg_link.im.protocol.data.InvalidPackageException;
import com.omg_link.im.protocol.data_pack.DataPack;

import java.util.UUID;

class ChatImagePack extends DataPack {
    private final UUID serverImageId;

    public ChatImagePack(UUID serverImageId) {
        super(Type.ChatImage);
        this.serverImageId = serverImageId;
    }

    public ChatImagePack(ByteData data) throws InvalidPackageException {
        super(data);
        assert(getType()== Type.ChatImage);
        this.serverImageId = data.decodeUuid();
    }

    @Override
    public ByteData encode() {
        return super.encode()
                .append(serverImageId);
    }

    public UUID getServerImageId() {
        return serverImageId;
    }

}
