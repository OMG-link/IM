package com.omg_link.im.protocol.file_transfer;

public class NoSuchTaskIdException extends Throwable{
    public NoSuchTaskIdException(){
        super();
    }

    public NoSuchTaskIdException(String reason){
        super(reason);
    }

}
