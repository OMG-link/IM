package protocol.dataPack;

import java.security.InvalidParameterException;

public enum ImageType {
    PNG,JPG;

    public int toId(){
        return ImageType.toId(this);
    }

    public static int toId(ImageType type){
        return type.ordinal();
    }

    public static ImageType toType(int id){
        if(id<0||id>=values().length){
            throw new InvalidParameterException(String.format("Invalid data pack type %d.",id));
        }
        return values()[id];
    }

}
