package im.file_manager;

import im.config.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class FileManager {
    private final Map<String, UUID> filePathToUuidMap = new HashMap<>();
    private final Map<UUID, FileObject> uuidToFileObjectMap = new HashMap<>();

    /**
     * @throws IOException When the target operation cannot be performed.
     */
    protected static void makeFolder(File folder,boolean shouldClearFolder) throws IOException {
        if(!folder.exists()){
            makeFolder(folder.getParentFile(),false);
            if(!folder.mkdir()){
                throw new IOException("Unable to create folder.");
            }
        }else{
            if(!folder.isDirectory()){
                throw new IOException("Target folder is a file.");
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

    /**
     * Create a file.
     * @param file The file you want to create.
     * @return A file object which is used to operate the file.
     * @throws IOException IO Error thrown by createNewFile().
     * @throws FileAlreadyExistsException When the file already exists.
     */
    public FileObject createFile(File file) throws FileAlreadyExistsException,IOException {
        if(!file.createNewFile()) {
            throw new FileAlreadyExistsException(file.getAbsolutePath());
        }
        return openFile(file);
    }

    /**
     * Create a file with random name in the given folder.
     * @param folderName The name of the folder you want to create file in.
     * @return A file object which is used to operate the file.
     * @throws IOException IO Error thrown by createNewFile().
     */
    public FileObject createUnnamedFileInFolder(String folderName) throws IOException {
        File file;
        do{
            file = new File(folderName+'/'+UUID.randomUUID());
        }while(!file.createNewFile());
        return openFile(file);
    }

    /**
     * Open a file.(Call createFile if you want to create a file).
     * @param file The file you want to open.
     * @return A file object which is used to operate the file.
     * @throws FileNotFoundException When the file does not exist.
     */
    public FileObject openFile(File file) throws FileNotFoundException {
        if(!file.exists()) {
            throw new FileNotFoundException();
        }
        String filePath = file.getAbsolutePath();
        if(filePathToUuidMap.containsKey(filePath)){
            return uuidToFileObjectMap.get(filePathToUuidMap.get(filePath));
        }else{
            UUID fileId = getNewFileId();
            FileObject fileObject = new FileObject(file,fileId);
            filePathToUuidMap.put(filePath,fileId);
            uuidToFileObjectMap.put(fileId,fileObject);
            return fileObject;
        }
    }

    /**
     * Open a file that has already been created in FileManager.
     * @param fileId An ID used to indicate the file.
     * @return A file object which is used to operate the file.
     * @throws NoSuchFileIdException When the UUID does not exist.
     */
    public FileObject openFile(UUID fileId) throws NoSuchFileIdException {
        if(uuidToFileObjectMap.containsKey(fileId)){
            return uuidToFileObjectMap.get(fileId);
        }else{
            throw new NoSuchFileIdException(fileId);
        }
    }

    /**
     * <p>
     *     Open a folder.
     * </p>
     * <p>
     *     If the folder does not exist, it will automatically try to create one.
     * </p>
     * @param relativeFolderPath The path relative to the program root.
     * @return The File of the folder.
     * @throws IOException When the target folder cannot be created or the folder is a file.
     */
    public File openFolder(String relativeFolderPath) throws IOException {
        String path = Config.getRuntimeDir()+relativeFolderPath;
        File file = new File(path);
        if(!file.exists()){
            makeFolder(file.getAbsoluteFile(),false);
        }
        if(!file.isDirectory()){
            throw new IOException("Target folder is a file.");
        }
        return file;
    }

    public void deleteFile(UUID fileId) throws NoSuchFileIdException,FileOccupiedException {
        if(uuidToFileObjectMap.containsKey(fileId)){
            deleteFile(uuidToFileObjectMap.get(fileId));
        }else{
            throw new NoSuchFileIdException(fileId);
        }
    }

    public void deleteFile(FileObject fileObject) throws FileOccupiedException {
        fileObject.delete();
        filePathToUuidMap.remove(fileObject.getFile().getAbsolutePath());
        uuidToFileObjectMap.remove(fileObject.getFileId());
    }

    private UUID getNewFileId() {
        UUID fileId;
        do{
            fileId = UUID.randomUUID();
        }while(uuidToFileObjectMap.containsKey(fileId));
        return fileId;
    }

}
