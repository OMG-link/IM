package protocol.dataPack;

import IM.Config;
import protocol.helper.data.ByteData;
import protocol.helper.data.InvalidPackageException;

public class CheckVersionPack extends DataPack {
    private String version;
    private String compatibleVersion;

    public CheckVersionPack(){
        super(DataPackType.CheckVersion);
        this.version = Config.version;
        this.compatibleVersion = Config.compatibleVersion;
    }

    public CheckVersionPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.CheckVersion);
        this.decode(data);
    }

    @Override
    public ByteData encode(){
        ByteData data = new ByteData();
        data.append(super.encode());
        data.append(ByteData.encode(version));
        data.append(ByteData.encode(compatibleVersion));
        return data;
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        ByteData backup = new ByteData(data);
        try{
            super.decode(data);
            this.version = data.decodeString();
            this.compatibleVersion = data.decodeString();
        }catch (InvalidPackageException e){
            data.setData(backup.getData());
            throw e;
        }
    }

    public String getVersion() {
        return version;
    }

    public String getCompatibleVersion() {
        return compatibleVersion;
    }

}
