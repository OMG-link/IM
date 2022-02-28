package protocol.dataPack;

import protocol.helper.data.ByteData;
import protocol.helper.data.InvalidPackageException;

public class PingPack extends DataPack{
    public PingPack(){
        super(DataPackType.Ping);
    }

    public PingPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.Ping);
        this.decode(data);
    }

}
