package mutil.file;

import IM.Config;
import mutil.uuidLocator.UuidConflictException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerFileManager extends FileManager{

    private static final String folderName = "UploadedFiles";

    private final Map<UUID,String> fileNameMapping = new HashMap<>();

    public ServerFileManager() throws IOException {
        makeTempFolder();
    }

    private void makeTempFolder() throws IOException {
        File folder = new File(getFolderName());
        if(!folder.exists()){
            if(!folder.mkdir()){
                throw new IOException("Unable to create file upload folder.");
            }
        }else{
            if(!folder.isDirectory()){
                throw new IOException("Unable to create file upload folder.");
            }
            File[] files = folder.listFiles();
            if(files==null){
                throw new IOException("Unable to clear file upload folder.");
            }
            for(File file:files){
                file.delete();
            }
        }
    }

    public FileObject createFile(UUID uuid,String fileName,long fileSize) throws UuidConflictException {
        try{
            FileObject temp = super.createFile(uuid,new File(getFolderName()+'/'+uuid),fileSize);
            //UUID has been checked when creating file, but I still want to check it.
            if(fileNameMapping.containsKey(uuid))
                throw new UuidConflictException();
            fileNameMapping.put(uuid,fileName);
            return temp;
        }catch (IOException e){
            //This should never happen!
            throw new RuntimeException(e);
        }
    }

    public FileObject openFile(UUID uuid) throws FileNotFoundException {
        try{
            return super.openFile(uuid,new File(getFolderName()+'/'+uuid));
        }catch (UuidConflictException e){
            //This should never happen!
            throw new RuntimeException(e);
        }
    }

    public String getFileName(UUID uuid) throws FileNotFoundException {
        if(!fileNameMapping.containsKey(uuid))
            throw new FileNotFoundException();
        return fileNameMapping.get(uuid);
    }

    @Override
    protected String getFolderName() {
        return Config.getRuntimeDir()+folderName;
    }

}
