package im.config;

public class ConfigSetFailedException extends Throwable{
    public ConfigSetFailedException(String reason){
        super(reason);
    }
}
