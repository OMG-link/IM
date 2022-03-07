package mutils.file;

import IM.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

public class FileObject {
    private final File file;
    private final UUID fileId;

    private int readInstanceCount;
    private int writeInstanceCount;

    public FileObject(File file,UUID fileId) {
        this.file = file;
        this.fileId = fileId;
        this.readInstanceCount = 0;
        this.writeInstanceCount = 0;
    }

    public File getFile() {
        return file;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void onReadInstanceCreate(){
        this.readInstanceCount++;
    }

    public void onWriteInstanceCreate(){
        this.writeInstanceCount++;
    }

    public void onReadInstanceClose(){
        this.readInstanceCount--;
    }

    public void onWriteInstanceClose(){
        this.writeInstanceCount--;
    }

    public void setLength(long value) throws IOException {
        if(value > Config.fileMaxSize) {
            throw new IOException("File too large!");
        }
        WriteOnlyFile writeOnlyFile = getWriteOnlyInstance();
        writeOnlyFile.setLength(value);
        writeOnlyFile.close();
    }

    public void delete() throws FileOccupiedException {
        if(this.readInstanceCount>0) {
            throw new FileOccupiedException("File delete failed: read instance unclosed.");
        }
        if(this.writeInstanceCount>0) {
            throw new FileOccupiedException("File delete failed: write instance unclosed.");
        }
        if(file.exists()){
            if(!file.delete()) {
                throw new FileOccupiedException("File delete failed: system error.");
            }
        }
    }

    public ReadOnlyFile getReadOnlyInstance() throws FileOccupiedException, FileNotFoundException {
        if (this.writeInstanceCount > 0)
            throw new FileOccupiedException();
        return new ReadOnlyFile(this);
    }

    public WriteOnlyFile getWriteOnlyInstance() throws FileOccupiedException, FileNotFoundException {
        if (this.readInstanceCount > 0 || this.writeInstanceCount > 0)
            throw new FileOccupiedException();
        return new WriteOnlyFile(this);
    }

}
