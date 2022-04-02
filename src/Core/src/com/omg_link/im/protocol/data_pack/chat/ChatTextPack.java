package com.omg_link.im.protocol.data_pack.chat;

import com.omg_link.im.protocol.data.ByteData;
import com.omg_link.im.protocol.data.InvalidPackageException;
import com.omg_link.im.protocol.data_pack.DataPack;

class ChatTextPack extends DataPack {
    private final String text;

    public ChatTextPack(String text){
        super(Type.ChatText);
        this.text = text;
    }

    public ChatTextPack(ByteData data) throws InvalidPackageException {
        super(data);
        assert(getType()== Type.ChatText);
        text = data.decodeString();
    }

    @Override
    public ByteData encode(){
        return super.encode()
                .append(text);
    }

    public String getText() {
        return text;
    }

}
