package protocol.dataPack;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

public enum DataPackType {
    Undefined,
    //System
    CheckVersion,Ping,
    //Chat
    Text, FileUploaded,
    //User list
    NameUpdate, UserList,
    //File transfer
    FileUploadRequest, FileUploadReply, FileDownloadRequest, FileDownloadReply, FileContent;

    private static boolean isInitialized = false;
    private static Map<Integer,DataPackType> map = new HashMap<>();

    public static int toId(DataPackType type){
        return type.ordinal();
    }

    public static DataPackType toType(int id){
        if(!isInitialized){
            for(DataPackType type:DataPackType.values()){
                map.put(type.ordinal(),type);
            }
            isInitialized = true;
        }
        DataPackType res = map.get(id);
        if(res==null) throw new InvalidParameterException();
        else return res;
    }

}
