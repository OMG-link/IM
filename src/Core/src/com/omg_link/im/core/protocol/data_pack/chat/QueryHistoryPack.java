package com.omg_link.im.core.protocol.data_pack.chat;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data_pack.DataPack;
import com.omg_link.im.core.protocol.data.InvalidPackageException;

public class QueryHistoryPack extends DataPack {

    private final long lastSerialId;

    public QueryHistoryPack(long lastSerialId){
        super(Type.QueryHistory);
        this.lastSerialId = lastSerialId;
    }

    public QueryHistoryPack(ByteData data) throws InvalidPackageException {
        super(data);
        this.lastSerialId = data.decodeLong();
    }

    @Override
    public ByteData encode() {
        return super.encode()
                .append(lastSerialId);
    }

    public long getLastSerialId() {
        return lastSerialId;
    }

}
