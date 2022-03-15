package im.protocol.data_pack.system;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;

public class PingPack extends DataPack {
    public PingPack(){
        super(DataPackType.Ping);
    }

    public PingPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.Ping);
        this.decode(data);
    }

}
