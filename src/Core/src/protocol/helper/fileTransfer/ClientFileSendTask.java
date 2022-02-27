package protocol.helper.fileTransfer;

import GUI.IFileTransferringPanel;
import IM.Client;
import mutil.file.FileObject;
import mutil.uuidLocator.UUIDManager;
import mutil.uuidLocator.UuidConflictException;
import protocol.dataPack.DataPack;
import protocol.helper.data.PackageTooLargeException;

import java.io.File;
import java.io.FileNotFoundException;

public class ClientFileSendTask extends FileSendTask{
    private final Client handler;
    private final IFileTransferringPanel panel;
    private final FileObject fileObject;
    private final String fileName;

    public ClientFileSendTask(Client handler,File file) throws FileNotFoundException {
        try{
            this.handler = handler;
            this.panel = handler.getRoomFrame().addFileTransferringPanel(file.getName());
            this.fileName = file.getName();
            this.fileObject = handler.getFileManager().openFile(getUuid(),file);
        }catch (UuidConflictException e){
            //Should not happen...
            throw new RuntimeException(e);
        }
    }

    public IFileTransferringPanel getPanel() {
        return panel;
    }

    @Override
    public FileObject getFileObject() {
        return fileObject;
    }

    @Override
    protected void send(DataPack dataPack) {
        try{
            this.handler.getNetworkHandler().send(dataPack);
        }catch (PackageTooLargeException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void showInfo(String info) {
        panel.setInfo("["+fileName+"] "+info);
    }

    @Override
    protected void setUploadProgress(double progress){
        panel.setProgress(progress);
    }

    @Override
    public UUIDManager getUuidManager() {
        return handler.getUuidManager();
    }

}
