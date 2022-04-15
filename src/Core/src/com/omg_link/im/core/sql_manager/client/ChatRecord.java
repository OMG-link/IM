package com.omg_link.im.core.sql_manager.client;

import com.omg_link.im.core.protocol.data.ByteData;

public class ChatRecord {
    public long serialId;
    public boolean isSelfSent;
    public ByteData data;

    public ChatRecord(long serialId, boolean isSelfSent, ByteData data){
        this.serialId = serialId;
        this.isSelfSent = isSelfSent;
        this.data = data;
    }

}
