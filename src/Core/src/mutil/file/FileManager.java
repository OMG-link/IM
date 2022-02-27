package mutil.file;

import mutil.uuidLocator.UuidConflictException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class FileManager {
    private Map<UUID, FileObject> map = new HashMap<>();

    abstract String getFolderName();

    public FileObject createFile(UUID uuid, File file,long fileSize) throws UuidConflictException, IOException {
        if(file.exists())
            throw new FileAlreadyExistsException(file.getName());
        if(!file.createNewFile())
            throw new IOException("Cannot create target file.");
        if(map.containsKey(uuid))
            throw new UuidConflictException();
        FileObject fileObject = new FileObject(file);
        map.put(uuid, fileObject);
        return fileObject;
    }

    public FileObject openFile(UUID uuid, File file) throws UuidConflictException, FileNotFoundException {
        if(!file.exists())
            throw new FileNotFoundException();
        if(map.containsKey(uuid)){
            if(!Objects.equals(map.get(uuid).getFile(),file))
                throw new UuidConflictException();
        }else{
            map.put(uuid, new FileObject(file));
        }
        return map.get(uuid);
    }

}
