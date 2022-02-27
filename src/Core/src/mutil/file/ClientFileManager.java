package mutil.file;

import IM.Config;
import mutil.uuidLocator.UuidConflictException;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.UUID;

public class ClientFileManager extends FileManager{

    private static final String folderName = "Download";

    public ClientFileManager() throws IOException {
        makeDownloadFolder();
    }

    private void makeDownloadFolder() throws IOException {
        File folder = new File(getFolderName());
        if(!folder.exists()){
            if(!folder.mkdir()){
                throw new IOException("Unable to create file upload folder.");
            }
        }
    }

    @Override
    protected String getFolderName(){
        return Config.getCacheDir()+folderName;
    }

    public FileObject createFile(UUID uuid,String fileName,long fileSize) throws UuidConflictException, IOException {
        File file = new File(getFolderName()+'/'+fileName);
        return super.createFile(uuid,file,fileSize);
    }

    public FileObject createFileRenameable(UUID uuid,String fileName,long fileSize) throws UuidConflictException, IOException {
        try{
            return createFile(uuid,fileName,fileSize);
        }catch (FileAlreadyExistsException e){
            for(int i=1;;i++){
                String replacedFileName = String.format("%d-%s",i,fileName);
                try{
                    return createFile(uuid,replacedFileName,fileSize);
                }catch (FileAlreadyExistsException ignored){
                }
                if(i>10000){
                    throw new IOException("Cannot create target file.(Cannot find a proper name)");
                }
            }
        }
    }

}
