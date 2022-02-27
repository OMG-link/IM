package mutil.file;

import java.io.IOException;

public class FileOccupiedException extends IOException {
    public FileOccupiedException(){
        super();
    }

    public FileOccupiedException(String reason){
        super(reason);
    }

}
