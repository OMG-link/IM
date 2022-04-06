package com.omg_link.im.core.protocol.data_pack.system;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data_pack.DataPack;

public abstract class CheckVersionPack extends DataPack {

    public CheckVersionPack(Type type) {
        super(type);
    }

    public CheckVersionPack(ByteData data) throws InvalidPackageException {
        super(data);
    }

    public abstract String getVersion();

    public abstract String getCompatibleVersion();

}
