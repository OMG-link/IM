package im.protocol;

/**
 * Thrown when trying to send data pack before version is checked.
 */
public class VersionNotCheckedException extends RuntimeException{
    public VersionNotCheckedException(){
        super();
    }
}
