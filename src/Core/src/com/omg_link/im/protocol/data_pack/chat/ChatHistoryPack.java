package com.omg_link.im.protocol.data_pack.chat;

import com.omg_link.im.protocol.data.ByteData;
import com.omg_link.im.protocol.data.InvalidPackageException;
import com.omg_link.im.protocol.data_pack.DataPack;

import java.util.ArrayList;
import java.util.Collection;

public class ChatHistoryPack extends DataPack {
    private final Collection<ByteData> packs = new ArrayList<>();

    public ChatHistoryPack(){
        super(Type.ChatHistory);
    }

    public ChatHistoryPack(ByteData data) throws InvalidPackageException {
        super(data);
        int cnt = data.decodeInt();
        while(cnt-->0){
            packs.add(new ByteData(data.decodeByteArray()));
        }
    }

    @Override
    public ByteData encode() {
        ByteData data = super.encode();
        data.append(packs.size());
        for(ByteData pData:packs){
            data.append(pData.getByteArray());
        }
        return data;
    }

    public void addPack(ByteData data){
        packs.add(data);
    }

    public Collection<ByteData> getPacks() {
        return packs;
    }

}
