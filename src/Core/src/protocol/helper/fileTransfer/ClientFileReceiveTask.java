package protocol.helper.fileTransfer;

import GUI.IFileTransferringPanel;
import IM.Client;
import mutil.file.WriteOnlyFile;
import mutil.uuidLocator.UUIDManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ClientFileReceiveTask extends FileReceiveTask {
    private final Client handler;
    private final IFileTransferringPanel panel;
    private final String fileName;

    public ClientFileReceiveTask(Client handler, WriteOnlyFile file, UUID uuid, long fileSize){
        super(uuid,file,fileSize);
        this.handler = handler;
        this.fileName = file.getFileObject().getFile().getName();
        this.panel = handler.getRoomFrame().addFileTransferringPanel(fileName);
    }

    @Override
    protected void showInfo(String info) {
        panel.setInfo(info);
    }

    @Override
    protected void setTransferProgress(double progress){
        panel.setProgress(progress);
    }

    @Override
    public UUIDManager getUuidManager() {
        return handler.getUuidManager();
    }

    @Override
    protected void onEndSuccess() {
        panel.setInfo(String.format("Download succeed: %s",fileName));
        File file = this.file.getFileObject().getFile();
        try{
            //Open file directly is dangerous, we just need to open the folder.
            if(System.getProperty("os.name").toUpperCase().contains("WINDOWS")){
                Runtime.getRuntime().exec("explorer /select,"+file.getPath());
            }else{
                Desktop.getDesktop().open(file.getParentFile());
            }
        }catch (IOException ignored){
        }
    }

    @Override
    protected void onEndFail() {
        panel.setInfo(String.format("Download failed: %s",fileName));
    }

}
