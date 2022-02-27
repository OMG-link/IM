package protocol.helper.fileTransfer;

import mutil.file.FileObject;
import mutil.file.ReadOnlyFile;
import protocol.dataPack.FileContentPack;
import protocol.dataPack.UploadRequestPack;
import protocol.dataPack.DataPack;
import mutil.uuidLocator.IUuidLocatable;

import java.io.IOException;
import java.util.UUID;

public abstract class FileSendTask implements IUuidLocatable, Runnable {
    protected final UUID uuid = UUID.randomUUID();

    public void start(){
        this.onCreate();
        try{
            this.sendUploadRequestPack();
        }catch (IOException e){
            this.end("Unable to send upload request pack.");
        }
    }

    private void end(){
        if(this instanceof ClientFileSendTask){
            ClientFileSendTask task = (ClientFileSendTask) this;
            task.getPanel().setVisible(false);
        }
        this.end("Ended.");
    }

    private void end(String reason){
        this.showInfo("Upload task ended: "+reason);
        this.onEnd();
        this.onDelete();
    }

    protected void onEnd(){
        try{
            this.sendUploadFinishPack();
        }catch (IOException e1){
            //do nothing
        }
    }

    abstract protected FileObject getFileObject();
    abstract void send(DataPack dataPack) throws IOException ;

    protected void showInfo(String info){}
    protected void setUploadProgress(double progress){}

    protected void sendUploadRequestPack() throws IOException {
        UploadRequestPack pack = new UploadRequestPack(
                getFileObject().getFile().getName(),
                getFileObject().getFile().length(),
                uuid
        );
        this.send(pack);
    }

    private void sendUploadFinishPack() throws IOException {
        FileContentPack pack = new FileContentPack(uuid,-1,new byte[0],0);
        this.send(pack);
    }

    public void onReceiveUploadReply(boolean result, String desc){
        if(result){
            Thread thread = new Thread(this,String.format("File Upload Task(%s)", uuid));
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
                FileContentPack pack = new FileContentPack(uuid,offset,buffer,length);
                this.send(pack);
                offset += length;
                this.setUploadProgress((double)offset/file.length());
            }
        }
    }

    @Override
    public UUID getUuid(){
        return uuid;
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
}
