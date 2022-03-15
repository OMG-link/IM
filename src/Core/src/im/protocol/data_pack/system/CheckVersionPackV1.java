package im.protocol.data_pack.system;

import im.config.Config;
import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;

/**
 * The older version of CheckVersionPack.
 */
public class CheckVersionPackV1 extends DataPack {
    private String version;

    public CheckVersionPackV1(){
        super(DataPackType.CheckVersion);
        this.version = Config.version;
    }

    /**
     * Constructor for ByteData.
     * This constructor will not change the parameter data if an InvalidPackageException was thrown.
     */
    public CheckVersionPackV1(ByteData data) throws InvalidPackageException {
        super(DataPackType.CheckVersion);
        this.decode(data);
    }

    @Override
    public ByteData encode(){
        ByteData data = new ByteData();
        data.append(super.encode());
        data.append(ByteData.encode(version));
        return data;
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        ByteData backup = new ByteData(data);
        try{
            super.decode(data);
            this.version = ByteData.decodeString(data);
        }catch (InvalidPackageException e){
            data.setData(backup);
            throw e;
        }
    }

    public String getVersion() {
        return version;
    }

}
