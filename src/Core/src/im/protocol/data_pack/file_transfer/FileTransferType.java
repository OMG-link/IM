package im.protocol.data_pack.file_transfer;

import java.security.InvalidParameterException;

public enum FileTransferType {
    ChatFile,ChatImage;

    public int toId(){
        return FileTransferType.toId(this);
    }

    public static int toId(FileTransferType type){
        return type.ordinal();
    }

    public static FileTransferType toType(int id){
        if(id<0||id>=values().length){
            throw new InvalidParameterException(String.format("Invalid data pack type %d.",id));
        }
        return values()[id];
    }

}
