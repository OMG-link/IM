package com.omg_link.im.core.protocol.data_pack.chat;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;

import java.util.UUID;

public class ChatTextSendPack extends ChatTextPack{
    private final UUID msgId;

    public ChatTextSendPack(UUID msgId,String text){
        super(text);
        this.msgId = msgId;
    }

    public ChatTextSendPack(ByteData data) throws InvalidPackageException {
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
