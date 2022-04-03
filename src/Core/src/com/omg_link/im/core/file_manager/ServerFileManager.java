package com.omg_link.im.core.file_manager;

import com.omg_link.im.core.config.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

public class ServerFileManager extends FileManager {

    private final String folderName;

    public ServerFileManager(UUID serverId) throws IOException {
        folderName = "UploadedFiles/" + serverId;
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

    @Override
    public FileObject openFile(UUID fileId) throws NoSuchFileIdException {
        try{
            var file = new File(getFolderName() + '/' + fileId);
            return super.openFile(file,fileId);
        }catch (FileNotFoundException e){
            throw new NoSuchFileIdException();
        }
    }

}
