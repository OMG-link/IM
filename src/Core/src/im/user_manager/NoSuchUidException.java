package im.user_manager;

public class NoSuchUidException extends Throwable{
    public NoSuchUidException(){
        super();
    }

    public NoSuchUidException(String reason){
        super(reason);
    }

}
