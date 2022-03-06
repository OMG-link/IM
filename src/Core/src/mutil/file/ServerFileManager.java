package mutil.file;

import IM.Config;

import java.io.File;
import java.io.FileNotFoundException;
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

    /**
     * Create a file on the server.
     * Note that you always create a new file with a different UUID, which means you must use the UUID to locate the file when needed.
     * @param fileName The actual name of this file, which is used to reply to client.
     * @return File object for the file.
     */
    public FileObject createFile(String fileName){
        try{
            FileObject fileObject = super.createFileInFolder(getFolderName());
            uuidToNameMap.put(fileObject.getFileId(),fileName);
            return fileObject;
        }catch (IOException e){
            //This should never happen!
            throw new RuntimeException(e);
        }
    }

    public FileObject openFile(UUID fileId) throws FileNotFoundException {
        return openFile(new File(getFolderName()+'/'+fileId.toString()));
    }

    public String getFileName(UUID uuid) throws FileNotFoundException {
        if(!uuidToNameMap.containsKey(uuid)) {
            throw new FileNotFoundException();
        }
        return uuidToNameMap.get(uuid);
    }

    public String getFolderName() {
        return Config.getCacheDir()+folderName;
    }

}
