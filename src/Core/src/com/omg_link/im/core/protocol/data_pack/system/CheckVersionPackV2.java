package com.omg_link.im.core.protocol.data_pack.system;

import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data_pack.DataPack;

public class CheckVersionPackV2 extends CheckVersionPack {
    private final String version;
    private final String compatibleVersion;

    public CheckVersionPackV2(){
        super(Type.CheckVersion);
        this.version = Config.version;
        this.compatibleVersion = Config.compatibleVersion;
    }

    public CheckVersionPackV2(ByteData data) throws InvalidPackageException {
        super(data);
        assert(getType()==Type.CheckVersion);
        this.version = data.decodeString();
        this.compatibleVersion = data.decodeString();
    }

    @Override
    public ByteData encode(){
        return super.encode()
                .append(version)
                .append(compatibleVersion);
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getCompatibleVersion() {
        return compatibleVersion;
    }

}
