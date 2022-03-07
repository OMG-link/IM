package protocol.helper.fileTransfer;

import GUI.IFileTransferringPanel;
import IM.Client;
import mutils.file.FileManager;
import mutils.uuidLocator.UUIDManager;
import protocol.dataPack.DataPack;
import protocol.dataPack.FileTransferType;
import protocol.dataPack.UploadRequestPack;
import protocol.helper.data.PackageTooLargeException;

import java.io.File;
import java.io.FileNotFoundException;

public class ClientFileSendTask extends FileSendTask {
    private final Client handler;
    private final IFileTransferringPanel panel;
    private final IUploadCallback callback;

    //constructors

    public ClientFileSendTask(
            Client handler,
            File file, FileTransferType fileTransferType,
            IFileTransferringPanel panel, IUploadCallback callback
    ) throws FileNotFoundException {
        super(fileTransferType);
        this.handler = handler;
        this.panel = panel;
        this.callback = callback;
        super.setSenderTaskId();

        super.setFile(file);

    }

    //abstract

    @Override
    protected void send(DataPack dataPack) throws PackageTooLargeException {
        this.handler.getNetworkHandler().send(dataPack);
    }

    @Override
    FileManager getFileManager() {
        return handler.getFileManager();
    }

    @Override
    public UUIDManager getUuidManager() {
        return handler.getUuidManager();
    }

    //start

    @Override
    public void start() {
        if(panel!=null){
            panel.onTransferStart();
        }
        super.start();
    }

    @Override
    void sendUploadRequestPack(){
        try{
            send(new UploadRequestPack(this));
        }catch (PackageTooLargeException e){
            throw new RuntimeException(e);
        }
    }

    //end

    @Override
    protected void onTransferEnd() {
        super.onTransferEnd();
    }

    @Override
    public void onEndSucceed() {
        if(panel!=null){
            panel.onTransferSucceed(getFile());
        }
        if (callback != null) {
            callback.onSucceed(this);
        }
        super.onEndSucceed();
    }

    @Override
    public void onEndFailed(String reason) {
        if(localEndReason!=null){ //use local end reason if exists
            reason = localEndReason;
        }
        if(panel!=null){
            panel.onTransferFailed(reason);
        }
        if (callback != null){
            callback.onFailed(this, reason);
        }
        super.onEndFailed(reason);
    }

    //functions

    @Override
    protected void onTransferProgressChange(long uploadedSize) {
        if(panel!=null){
            panel.setProgress(uploadedSize);
        }
    }

    //get

    public IFileTransferringPanel getPanel() {
        return panel;
    }

}
