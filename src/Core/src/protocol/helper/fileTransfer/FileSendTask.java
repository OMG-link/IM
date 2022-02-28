package protocol.helper.fileTransfer;

import mutil.file.FileObject;
import mutil.file.ReadOnlyFile;
import mutil.uuidLocator.IUuidLocatable;
import mutil.uuidLocator.UuidConflictException;
import protocol.dataPack.DataPack;
import protocol.dataPack.FileContentPack;
import protocol.dataPack.FileTransferType;
import protocol.dataPack.UploadRequestPack;
import protocol.helper.data.PackageTooLargeException;

import java.io.IOException;
import java.util.UUID;

public abstract class FileSendTask implements IUuidLocatable, Runnable {
    /**
     * This uuid is generated randomly.
     */
    protected UUID senderTaskId;
    protected UUID receiverTaskId = null;

    void init(){
        while(true){
            try{
                senderTaskId = UUID.randomUUID();
                onCreate();
                break;
            }catch (UuidConflictException ignored){ //try again
            }
        }
    }

    public void start(){
        try{
            this.sendUploadRequestPack();
        }catch (IOException e){
            this.end("Unable to send upload request pack.");
        }
    }

    protected void end(){
        this.end("Ended.");
    }

    private void end(String reason){
        this.showInfo("Upload task ended: "+reason);
        this.onEnd();
    }

    protected void onEnd(){
        try{
            this.sendUploadFinishPack();
        }catch (IOException e1){
            //do nothing
        }
    }

    public void onEndSucceed(){
        this.onDelete();
    }
    public void onEndFailed(String reason){
        this.onDelete();
    }

    abstract protected UUID getFileId();
    abstract protected String getFileName();
    abstract protected FileTransferType getFileTransferType();
    abstract protected FileObject getFileObject();
    abstract void send(DataPack dataPack) throws IOException, PackageTooLargeException;

    protected void showInfo(String info){}
    protected void setUploadProgress(double progress){}

    protected void sendUploadRequestPack() throws IOException {
        UploadRequestPack pack = new UploadRequestPack(
                getFileName(),
                getFileObject().getFile().length(),
                getSenderTaskId(),
                getReceiverTaskId(),
                getFileTransferType()
        );
        try{
            this.send(pack);
        }catch (PackageTooLargeException e){
            throw new RuntimeException(e);
        }
    }

    private void sendUploadFinishPack() throws IOException {
        FileContentPack pack = new FileContentPack(getReceiverTaskId(),-1,new byte[0],0);
        try{
            this.send(pack);
        }catch (PackageTooLargeException e){
            throw new RuntimeException(e);
        }
    }

    public void onReceiveUploadReply(boolean result, String desc){
        if(result){
            Thread thread = new Thread(this,String.format("File Upload Task(%s)", getSenderTaskId()));
            thread.start();
        }else{
            this.end("Upload request was rejected: "+desc);
        }
    }

    private void sendDataLoop() throws IOException {
        try(ReadOnlyFile file = getFileObject().getReadOnlyInstance()){
            long offset = 0;
            byte[] buffer = new byte[FileContentPack.packSize];
            while(true){
                int length = file.read(buffer);
                if(length==-1) break;
                FileContentPack pack = new FileContentPack(getReceiverTaskId(),offset,buffer,length);
                try{
                    this.send(pack);
                }catch (PackageTooLargeException e){
                    throw new RuntimeException(e);
                }
                offset += length;
                this.setUploadProgress((double)offset/file.length());
            }
        }
    }

    @Override
    public UUID getUuid(){
        return getSenderTaskId();
    }

    @Override
    public void run() {
        try{
            sendDataLoop();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            this.end();
        }
    }

    public UUID getSenderTaskId() {
        return senderTaskId;
    }

    public void setReceiverTaskId(UUID receiverTaskId) {
        this.receiverTaskId = receiverTaskId;
    }

    public UUID getReceiverTaskId() {
        if(receiverTaskId==null){
            throw new RuntimeException("Receiver task id is not set yet!");
        }
        return receiverTaskId;
    }

}
