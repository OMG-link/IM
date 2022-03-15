package im.protocol.data_pack;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;

public abstract class DataPack {
    private int type;

    public DataPack(DataPackType type){
        this.setType(type);
    }

    public static boolean canDecode(ByteData data){
        try{
            int length = ByteData.peekInt(data);
            return data.getLength()>=length+4;
        }catch(InvalidPackageException e){
            return false;
        }
    }

    public ByteData encode(){
        ByteData data = new ByteData();
        data.append(ByteData.encode(type));
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
