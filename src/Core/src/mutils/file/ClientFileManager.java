package mutils.file;

import IM.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.UUID;

public class ClientFileManager extends FileManager{

    private static final String folderName = "Download";

    public ClientFileManager() throws IOException {
        makeDownloadFolder();
    }

    private void makeDownloadFolder() throws IOException {
        makeFolder(new File(getFolderName()).getAbsoluteFile(),false);
        makeFolder(new File(getCacheFolderName()).getAbsoluteFile(),true);
    }

    public String getFolderName(){
        return Config.getRuntimeDir()+folderName;
    }

    public String getCacheFolderName(){
        return Config.getCacheDir()+folderName;
    }

    private FileObject createFile(String folder,String fileName) throws IOException {
        File file = new File(folder+'/'+fileName);
        return super.createFile(file);
    }

    private FileObject createFileRenameable(String folder,String fileName) throws IOException {
        try{
            return createFile(folder,fileName);
        }catch (FileAlreadyExistsException e){
            for(int i=1;;++i){
                String replacedFileName = String.format("%d-%s",i,fileName);
                try{
                    return createFile(folder,replacedFileName);
                }catch (FileAlreadyExistsException ignored){
                }
                if(i>10000){
                    throw new IOException("Cannot create target file.(Cannot find a proper name)");
                }
            }
        }
    }

    public FileObject createFileRenameable(String fileName) throws IOException {
        return createFileRenameable(getFolderName(),fileName);
    }

    public FileObject createCacheFileRenameable(String fileName) throws IOException {
        return createFileRenameable(getCacheFolderName(),fileName);
    }

    public String getFileName(UUID fileId) throws FileNotFoundException {
        return openFile(fileId).getFile().getName();
    }

}
