package protocol.fileTransfer;

public class NoSuchTaskIdException extends Throwable{
    public NoSuchTaskIdException(){
        super();
    }

    public NoSuchTaskIdException(String reason){
        super(reason);
    }

}
