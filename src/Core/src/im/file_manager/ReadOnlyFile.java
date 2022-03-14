package im.file_manager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReadOnlyFile implements AutoCloseable {
    private final FileObject fileObject;
    private final RandomAccessFile file;
    private long ptr;
    private boolean isActive;

    public ReadOnlyFile(FileObject fileObject) throws FileNotFoundException {
        this.file = new RandomAccessFile(fileObject.getFile(),"r");
        this.ptr = 0L;
        this.fileObject = fileObject;
        this.fileObject.onReadInstanceCreate();
        this.isActive = true;
    }

    public long length(){
        return fileObject.getFile().length();
    }

    public int read(long fileOffset,byte[] data,int dataOffset,int dataLength) throws IOException {
        if(!isActive)
            throw new IOException("Instance has been closed.");
        if(dataLength<0) return 0;
        if(fileOffset>file.length())
            throw new IOException("Trying to read data out of file length.");
        file.seek(fileOffset);
        return file.read(data,dataOffset,dataLength);
    }

    public int read(long fileOffset,byte[] data) throws IOException {
        return read(fileOffset,data,0,data.length);
    }

    public int read(byte[] data) throws IOException {
        int length = read(ptr,data);
        ptr += length;
        return length;
    }

    public void close(){
        if(!isActive){
            Logger.getGlobal().log(Level.WARNING,"Read only file closed twice.");
            return;
        }
        try{this.file.close();}catch (IOException ignored){}
        this.fileObject.onReadInstanceClose();
        this.isActive = false;
    }

    public FileObject getFileObject() {
        return fileObject;
    }

}
