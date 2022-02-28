package protocol.dataPack;

import protocol.helper.data.ByteData;
import protocol.helper.data.InvalidPackageException;

public abstract class DataPack {
    private int type;

    public DataPack(DataPackType type){
        this.setType(type);
    }

    public static boolean canDecode(ByteData data){
        try{
            int length = ByteData.peekInt(data);
            return data.length()>=length+4;
        }catch(InvalidPackageException e){
            return false;
        }
    }

    public ByteData encode(){
        ByteData data = new ByteData();
        data.append(new ByteData(type));
        return data;
    }

    public void decode(ByteData data) throws InvalidPackageException {
        this.type = ByteData.decodeInt(data);
    }

    public DataPackType getType() {
        return DataPackType.toType(this.type);
    }

    public void setType(DataPackType type) {
        this.type = DataPackType.toId(type);
    }

}
