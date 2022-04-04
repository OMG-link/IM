package com.omg_link.im.core.file_manager;

import com.omg_link.im.core.ServerRoom;
import com.omg_link.im.core.config.Config;
import com.omg_link.utils.Sha512Digest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerFileManager extends FileManager {

    private final ServerRoom serverRoom;

    private final String folderName;
    private final Map<Sha512Digest,UUID> digestToUuidMap;
    private final Map<UUID,Sha512Digest> uuidToDigestMap = new HashMap<>();

    public ServerFileManager(ServerRoom serverRoom,UUID serverId) throws IOException {
        this.serverRoom = serverRoom;
        this.folderName = "UploadedFiles/" + serverId;

        var sqlManager = serverRoom.getSqlManager();
        if(sqlManager==null){
            this.digestToUuidMap = new HashMap<>();
        }else{
            Map<Sha512Digest,UUID> map;
            try{
                map = sqlManager.getFileDigestMapping();
            }catch (SQLException e){
                e.printStackTrace();
                serverRoom.closeSqlManager();
                map = new HashMap<>();
            }
            this.digestToUuidMap = map;
            for(var pair:map.entrySet()){
                uuidToDigestMap.put(pair.getValue(),pair.getKey());
            }
        }

        makeFolder(new File(getFolderName()).getAbsoluteFile());

    }

    private String getFolderName() {
        return Config.getCacheDir() + folderName;
    }

    /**
     * <p>Create a file on the server.</p>
     * <p>Note that you always create a new file with a different UUID, which means you must use the UUID to locate the file when needed.</p>
     *
     * @return File object for the file.
     */
    public FileObject createFile() {
        try {
            return createUnnamedFileInFolder(getFolderName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>Open a file through file ID.</p>
     * <p>This function will open a file whose name is exactly the UUID.</p>
     *
     * @param fileId An ID used to indicate the file.
     * @return A file object which is used to operate the file.
     * @throws NoSuchFileIdException When the file is not found.
     */
    @Override
    public FileObject openFile(UUID fileId) throws NoSuchFileIdException {
        try{
            var file = new File(getFolderName() + '/' + fileId);
            return super.openFile(file,fileId);
        }catch (FileNotFoundException e){
            throw new NoSuchFileIdException();
        }
    }

    /**
     * Check whether the file has been uploaded before.
     *
     * @param digest The digest of the file.
     * @return True if the file has already been uploaded.
     */
    public boolean isFileUploaded(Sha512Digest digest){
        return digestToUuidMap.containsKey(digest);
    }

    /**
     * Get file ID by a digest.
     *
     * @param digest The digest of the target file.
     * @return If the digest matches a file, return the file ID of it. Otherwise, return null.
     */
    public UUID getFileIdByDigest(Sha512Digest digest){
        return digestToUuidMap.get(digest);
    }

    public Sha512Digest getDigestByFileId(UUID fileId){
        return uuidToDigestMap.get(fileId);
    }

    public void addDigestMapping(Sha512Digest digest, UUID fileId){
        digestToUuidMap.put(digest,fileId);
        uuidToDigestMap.put(fileId,digest);
        var sqlManager = serverRoom.getSqlManager();
        if(sqlManager!=null){
            try{
                sqlManager.setFileDigestMapping(digest,fileId);
            }catch (SQLException e){
                serverRoom.closeSqlManager();
            }
        }
    }

}
