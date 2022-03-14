package im.file_manager;

import im.config.Config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerFileManager extends FileManager{

    private static final String folderName = "UploadedFiles";

    private final Map<UUID,String> uuidToNameMap = new HashMap<>();

    public ServerFileManager() throws IOException {
        makeTempFolder();
    }

    private void makeTempFolder() throws IOException {
        makeFolder(new File(getFolderName()).getAbsoluteFile(),true);
    }

    private String getFolderName() {
        return Config.getCacheDir()+folderName;
    }

    /**
     * Create a file on the server.
     * Note that you always create a new file with a different UUID, which means you must use the UUID to locate the file when needed.
     * @param fileName The actual name of this file, which is used to reply to client.
     * @return File object for the file.
     */
    public FileObject createFile(String fileName){
        try{
            FileObject fileObject = super.createUnnamedFileInFolder(getFolderName());
            uuidToNameMap.put(fileObject.getFileId(),fileName);
            return fileObject;
        }catch (IOException e){
            //This should never happen!
            throw new RuntimeException(e);
        }
    }

    public String getFileName(UUID uuid) throws NoSuchFileIdException {
        if(!uuidToNameMap.containsKey(uuid)) {
            throw new NoSuchFileIdException();
        }
        return uuidToNameMap.get(uuid);
    }

}
