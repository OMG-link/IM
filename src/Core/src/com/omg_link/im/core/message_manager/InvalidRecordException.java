package com.omg_link.im.core.message_manager;

/**
 * Thrown when a record cannot be recorded into the database.
 */
public class InvalidRecordException extends Exception{

    public InvalidRecordException(){
        super();
    }

    public InvalidRecordException(String reason){
        super(reason);
    }

}
