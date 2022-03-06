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
    private final Map<UUID, FileObject> uuidToFileObjectMap = new HashMap<>();
    private final Map<File, UUID> fileToUuidMap = new HashMap<>();

    protected static void makeFolder(File folder,boolean shouldClearFolder) throws IOException {
        if(!folder.exists()){
            makeFolder(folder.getParentFile(),false);
            if(!folder.mkdir()){
                throw new IOException("Unable to create folder.");
            }
        }else{
            if(!folder.isDirectory()){
                throw new IOException("Unable to create folder.");
            }
            if(shouldClearFolder){
                File[] files = folder.listFiles();
                if(files==null){
                    throw new IOException("Unable to clear folder.");
                }
                for(File file:files){
                    file.delete();
                }
            }
        }
    }

    public FileObject createFile(File file) throws IOException {
        if(file.exists()) {
            throw new FileAlreadyExistsException(file.getName());
        }
        if(!file.createNewFile()) {
            throw new IOException("Cannot create target file.");
        }
        return openFile(file);
    }

    /**
     * Create a file with random name in the given folder.
     * @param folderName The name of the folder you want to create file in.
     * @return A file object which is used to operate the file.
     * @throws IOException when file cannot be created. (Maybe permission or other reason)
     */
    public FileObject createFileInFolder(String folderName) throws IOException {
        UUID fileId = getNewFileId();
        File file = new File(folderName+'/'+fileId);
        if(!file.createNewFile()){
            throw new IOException("Cannot create target file.");
        }
        try{
            return openFile(file,fileId);
        }catch (UuidConflictException e){
            //file id is generated by getNewFileId, should not meet this exception
            throw new RuntimeException(e);
        }
    }

    private FileObject openFile(File file,UUID fileId) throws UuidConflictException {
        FileObject fileObject;
        if(uuidToFileObjectMap.containsKey(fileId)){
            fileObject = uuidToFileObjectMap.get(fileId);
            if(!Objects.equals(fileObject.getFile(),file)){
                throw new UuidConflictException();
            }
        }else{
            fileObject = new FileObject(file, fileId);
            fileToUuidMap.put(file,fileId);
            uuidToFileObjectMap.put(fileId,fileObject);
        }
        return fileObject;
    }

    public FileObject openFile(File file) throws FileNotFoundException {
        if(!file.exists()) {
            throw new FileNotFoundException();
        }
        if(!fileToUuidMap.containsKey(file)){
            UUID fileId = getNewFileId();
            fileToUuidMap.put(file,fileId);
        }
        try{
            return openFile(file,fileToUuidMap.get(file));
        }catch (UuidConflictException e){
            //file id is generated by getNewFileId, should not meet this exception
            throw new RuntimeException(e);
        }
    }

    public FileObject openFile(UUID fileId) throws FileNotFoundException {
        if(uuidToFileObjectMap.containsKey(fileId)){
            return uuidToFileObjectMap.get(fileId);
        }else{
            throw new FileNotFoundException(String.format("No file with UUID=%s",fileId));
        }
    }

    private UUID getNewFileId() {
        UUID fileId;
        do{
            fileId = UUID.randomUUID();
        }while(uuidToFileObjectMap.containsKey(fileId));
        return fileId;
    }

    public FileObject getFile(UUID uuid){
        return uuidToFileObjectMap.get(uuid);
    }

}
