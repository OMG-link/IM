package com.omg_link.im.core.protocol.data_pack.chat;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data_pack.DataPack;

public class SelfSentNotice extends DataPack {

    private final long serialId;

    public SelfSentNotice(long serialId) {
        super(Type.SelfSentNotice);
        this.serialId = serialId;
    }

    public SelfSentNotice(ByteData data) throws InvalidPackageException {
        super(data);
        this.serialId = data.decodeLong();
    }

    @Override
    public ByteData encode() {
        return super.encode()
                .append(serialId);
    }

    public long getSerialId() {
        return serialId;
    }

}
