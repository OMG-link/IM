package com.omg_link.im.core.protocol.data_pack.system;

import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;

/**
 * The older version of CheckVersionPack.
 */
public class CheckVersionPackV1 extends CheckVersionPack {
    private final String version;

    public CheckVersionPackV1(){
        super(Type.CheckVersion);
        this.version = Config.version;
    }

    public CheckVersionPackV1(ByteData data) throws InvalidPackageException {
        super(data);
        assert(getType()==Type.CheckVersion);
        this.version = data.decodeString();
    }

    @Override
    public ByteData encode(){
        return super.encode()
                .append(version);
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getCompatibleVersion() {
        return null;
    }

}
