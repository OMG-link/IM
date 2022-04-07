package com.omg_link.im.core.file_manager;

import com.omg_link.im.core.ClientRoom;
import com.omg_link.im.core.config.Config;
import com.omg_link.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientFileManager extends FileManager{
    public static final String downloadFolder = "Download";

    private ClientRoom room;
    private String cacheFolder;

    private boolean enableSql;

    public ClientFileManager() {
        this.cacheFolder = "{cache}/"
                .replace("{cache}",Config.getCacheDir());
        this.enableSql = false;
    }

    public void setToRoom(ClientRoom room, UUID serverId, boolean enableSql){
        this.room = room;
        this.cacheFolder = "{cache}/{serverId}/"
                .replace("{cache}",Config.getCacheDir())
                .replace("{serverId}",serverId.toString());
        this.enableSql = enableSql;

        if(enableSql){
            try{
                this.fileIdToPathMap = this.room.getSqlManager().getFileMapping();
            }catch (SQLException e){
                e.printStackTrace();
                disableSql();
            }
        }
        if(!enableSql){
            this.fileIdToPathMap = new HashMap<>();
        }

    }

    public void disableSql(){
        enableSql = false;
    }

    public String getCacheFolderName(){
        return cacheFolder;
    }

    private FileObject createFile(String folderName,String fileName) throws IOException {
        FileUtils.makeFolder(folderName);
        File file = new File(folderName+'/'+fileName);
        return super.createFile(file);
    }

    public FileObject createFileRenameable(String folder,String fileName) throws IOException {
        folder = Config.getRuntimeDir()+'/'+folder;
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

    public FileObject createCacheFile() throws IOException {
        return createCacheFile("Cache");
    }

    public FileObject createCacheFile(String folder) throws IOException {
        while(true){
            UUID uuid = UUID.randomUUID();
            try{
                return createFile(getCacheFolderName()+'/'+folder,uuid.toString());
            }catch (FileAlreadyExistsException ignored){}
        }
    }

    public String getFileName(UUID fileId) throws NoSuchFileIdException {
        return openFile(fileId).getFile().getName();
    }

    private Map<UUID,File> fileIdToPathMap;

    /**
     * <p>Get whether the file has been downloaded.</p>
     * <p>This function will check whether the file actually exists(Instead of just check it in database).</p>
     * @param serverFileId File ID on server.
     * @return True if the file has been downloaded and actually exists.
     */
    public boolean isFileDownloaded(UUID serverFileId){
        if(fileIdToPathMap.containsKey(serverFileId)){
            File file = fileIdToPathMap.get(serverFileId);
            if(file.exists()) return true;
            else{
                removeMapping(serverFileId);
                return false;
            }
        }else{
            return false;
        }
    }

    public FileObject openFileByServerFileId(UUID serverFileId) throws FileNotFoundException {
        if(!fileIdToPathMap.containsKey(serverFileId)){
            throw new FileNotFoundException();
        }
        return openFile(fileIdToPathMap.get(serverFileId));
    }

    public void addMapping(UUID serverFileId, File file){
        fileIdToPathMap.put(serverFileId,file);
        if(enableSql){
            try{
                room.getSqlManager().addFileMapping(serverFileId,file);
            }catch (SQLException e){
                e.printStackTrace();
                disableSql();
            }
        }
    }

    public void removeMapping(UUID serverFileId){
        fileIdToPathMap.remove(serverFileId);
        if(enableSql){
            try{
                room.getSqlManager().removeFileMapping(serverFileId);
            }catch (SQLException e){
                e.printStackTrace();
                disableSql();
            }
        }
    }

    public static String getCacheFolderName(UUID serverId){
        return "{cache}/{serverId}"
                .replace("{cache}",Config.getCacheDir())
                .replace("{serverId}",serverId.toString());
    }

}
