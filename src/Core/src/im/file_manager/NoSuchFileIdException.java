package im.file_manager;

public class NoSuchFileIdException extends Throwable{
    public NoSuchFileIdException(){
        super();
    }

    public NoSuchFileIdException(String reason){
        super(reason);
    }

}
