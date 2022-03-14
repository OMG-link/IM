package protocol.dataPack;

import IM.Config;
import protocol.helper.data.ByteData;
import protocol.helper.data.InvalidPackageException;

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
            data.setData(backup.getData());
            throw e;
        }
    }

    public String getVersion() {
        return version;
    }

}
