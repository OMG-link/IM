package mutil.file;

import IM.Config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WriteOnlyFile implements AutoCloseable {
    private final FileObject fileObject;
    private final RandomAccessFile file;
    private long ptr;
    private boolean isActive;
    private long length;

    public WriteOnlyFile(FileObject fileObject) throws FileNotFoundException {
        this.file = new RandomAccessFile(fileObject.getFile(),"rw");
        this.ptr = 0L;
        this.fileObject = fileObject;
        this.fileObject.onWriteInstanceCreate();
        this.length = fileObject.getFile().length();
        this.isActive = true;
    }

    public long fileLength(){
        return length;
    }

    public void write(long fileOffset,byte[] data,int dataOffset,int dataLength) throws IOException {
        if(!isActive)
            throw new IOException("Instance has been closed.");
        if(dataLength<0) return;
        if(fileOffset<0||fileOffset>file.length())
            throw new IOException("Illegal write starting point.");
        file.seek(fileOffset);
        file.write(data,dataOffset,dataLength);
    }

    public void write(long fileOffset,byte[] data) throws IOException {
        write(fileOffset,data,0,data.length);
    }

    public void write(byte[] data) throws IOException {
        write(ptr,data);
        ptr += data.length;
    }

    /**
     * @throws IOException When the file was created failed.
     */
    public void setLength(long value) throws IOException {
        if(value > Config.fileMaxSize)
            throw new IOException("File too large!");
        length = value;
        file.setLength(value);
    }

    public void close(){
        if(!isActive){
            Logger.getLogger("IMCore").log(Level.WARNING,"Write only file closed twice.");
            return;
        }
        try{this.file.close();}catch (IOException ignored){}
        this.fileObject.onWriteInstanceClose();
        this.isActive = false;
    }

    public FileObject getFileObject() {
        return fileObject;
    }

}
