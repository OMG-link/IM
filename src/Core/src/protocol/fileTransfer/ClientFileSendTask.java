package protocol.fileTransfer;

import GUI.IFileTransferringPanel;
import IM.Client;
import mutils.file.FileManager;
import protocol.dataPack.DataPack;
import protocol.dataPack.FileTransferType;
import protocol.dataPack.UploadRequestPack;
import protocol.helper.data.PackageTooLargeException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.UUID;

public class ClientFileSendTask extends FileSendTask {
    private final Client client;
    private final IFileTransferringPanel panel;
    private final IUploadCallback callback;

    //constructors

    public ClientFileSendTask(
            Client client, UUID senderTaskId,
            File file, FileTransferType fileTransferType,
            IFileTransferringPanel panel, IUploadCallback callback
    ) throws FileNotFoundException {
        super(fileTransferType);
        this.client = client;
        this.panel = panel;
        this.callback = callback;
        super.setSenderTaskId(senderTaskId);

        super.setFile(file);

    }

    //abstract

    @Override
    protected void send(DataPack dataPack) throws PackageTooLargeException {
        this.client.getNetworkHandler().send(dataPack);
    }

    @Override
    FileManager getFileManager() {
        return client.getFileManager();
    }

    @Override
    void removeFromFactory() {
        try{
            client.getFactoryManager().getFileSendTaskFactory().remove(this);
        }catch (NoSuchTaskIdException e){
            throw new RuntimeException(e);
        }
    }

    //start

    @Override
    public void start() {
        if (panel != null) {
            panel.onTransferStart();
        }
        super.start();
    }

    @Override
    void sendUploadRequestPack() {
        try {
            send(new UploadRequestPack(this));
        } catch (PackageTooLargeException e) {
            throw new RuntimeException(e);
        }
    }

    //end

    @Override
    public void onEndSucceed() {
        if (panel != null) {
            panel.onTransferSucceed(getFile());
        }
        if (callback != null) {
            callback.onSucceed(this);
        }
        super.onEndSucceed();
    }

    @Override
    public void onEndFailed(String reason) {
        if (localEndReason != null) { //use local end reason if exists
            reason = localEndReason;
        }
        if (panel != null) {
            panel.onTransferFailed(reason);
        }
        if (callback != null) {
            callback.onFailed(this, reason);
        }
        super.onEndFailed(reason);
    }

    //functions

    @Override
    protected void onTransferProgressChange(long uploadedSize) {
        if (panel != null) {
            panel.setProgress(uploadedSize);
        }
    }

    //get

    public IFileTransferringPanel getPanel() {
        return panel;
    }

}
