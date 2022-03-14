package mutils.file;

public class NoSuchFileIdException extends Throwable{
    public NoSuchFileIdException(){
        super();
    }

    public NoSuchFileIdException(String reason){
        super(reason);
    }

}
