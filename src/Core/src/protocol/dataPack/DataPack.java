package protocol.dataPack;

import protocol.helper.data.Data;
import protocol.helper.data.InvalidPackageException;

public abstract class DataPack {
    private int type;

    public DataPack(){
        this(DataPackType.Undefined);
    }

    public DataPack(DataPackType type){
        this.setType(type);
    }

    public static boolean canDecode(Data data){
        try{
            int length = Data.peekInt(data);
            return data.length()>=length+4;
        }catch(InvalidPackageException e){
            return false;
        }
    }

    public Data encode(){
        Data data = new Data();
        data.append(new Data(type));
        return data;
    }

    public void decode(Data data) throws InvalidPackageException {
        this.type = Data.decodeInt(data);
    }

    public DataPackType getType() {
        return DataPackType.toType(this.type);
    }

    public void setType(DataPackType type) {
        this.type = DataPackType.toId(type);
    }

}
