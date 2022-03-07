package mutils.uuidLocator;

public class UuidConflictException extends Throwable{
    public UuidConflictException(){
        super();
    }

    public UuidConflictException(String reason){
        super(reason);
    }

}
