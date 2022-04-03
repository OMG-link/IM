package com.omg_link.im.core.protocol.data_pack.chat;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;

import java.util.UUID;

public class ChatImageSendPack extends ChatImagePack {

    private final UUID msgId;

    public ChatImageSendPack(UUID msgId,UUID serverImageId){
        super(serverImageId);
        this.msgId = msgId;
    }

    public ChatImageSendPack(ByteData data) throws InvalidPackageException {
        super(data);
        this.msgId = data.decodeUuid();
    }

    @Override
    public ByteData encode() {
        return super.encode()
                .append(msgId);
    }

    public UUID getMsgId() {
        return msgId;
    }

}
