package im.file_manager;

import im.config.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.UUID;

public class ClientFileManager extends FileManager{
    public static final String downloadFolder = "Download";

    public ClientFileManager() throws IOException {
        clearCacheFolder();
    }

    private void clearCacheFolder() throws IOException {
        makeFolder(new File(getCacheFolderName()).getAbsoluteFile(),true);
    }

    private void makeFolder(String folderName) throws IOException {
        File folder = new File(folderName);
        if(folder.exists()) return;
        makeFolder(folder.getAbsoluteFile(),false);
    }

    public String getCacheFolderName(){
        return Config.getCacheDir()+downloadFolder;
    }

    private FileObject createFile(String folder,String fileName) throws IOException {
        File file = new File(folder+'/'+fileName);
        return super.createFile(file);
    }

    public FileObject createFileRenameable(String folder,String fileName) throws IOException {
        folder = Config.getRuntimeDir()+folder;
        makeFolder(folder);
        try{
            return createFile(folder,fileName);
        }catch (FileAlreadyExistsException e){
            for(int i=1;;++i){
                String replacedFileName = String.format("%d-%s",i,fileName);
                try{
                    return createFile(folder,replacedFileName);
                }catch (FileAlreadyExistsException ignored){}
                if(i>10000){
                    throw new IOException("Cannot create target file.(Cannot find a proper name)");
                }
            }
        }
    }

    public FileObject createCacheFileRenameable(String fileName) throws IOException {
        return createFileRenameable(getCacheFolderName(),fileName);
    }

    public String getFileName(UUID fileId) throws NoSuchFileIdException {
        return openFile(fileId).getFile().getName();
    }

}
