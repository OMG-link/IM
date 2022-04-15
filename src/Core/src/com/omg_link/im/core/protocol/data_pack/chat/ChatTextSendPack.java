package com.omg_link.im.core.protocol.data_pack.chat;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;

public class ChatTextSendPack extends ChatTextPack{

    public ChatTextSendPack(String text){
        super(text);
    }

    public ChatTextSendPack(ByteData data) throws InvalidPackageException {
        super(data);
    }

    @Override
    public ByteData encode() {
        return super.encode();
    }

}
