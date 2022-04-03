package com.omg_link.im.core.protocol.data_pack.system;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data_pack.DataPack;

public class PingPack extends DataPack {
    public PingPack(){
        super(Type.Ping);
    }

    public PingPack(ByteData data) throws InvalidPackageException {
        super(data);
    }

}
