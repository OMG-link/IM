package com.omg_link.im.file_manager;

import java.util.UUID;

public class NoSuchFileIdException extends Throwable{
    public NoSuchFileIdException(){
        super();
    }

    public NoSuchFileIdException(String reason){
        super(reason);
    }

    public NoSuchFileIdException(UUID fileId){
        this(String.format("No file with UUID=%s",fileId));
    }

}
