package protocol.helper.fileTransfer;

import mutil.file.FileOccupiedException;
import mutil.file.WriteOnlyFile;
import mutil.uuidLocator.IUuidLocatable;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class FileReceiveTask implements IUuidLocatable {
    protected final UUID uuid;
    protected final long fileSize;
    private long transferredSize = 0;

    protected final WriteOnlyFile file;

    protected abstract void onEndSuccess();
    protected abstract void onEndFail();

    public FileReceiveTask(UUID uuid,WriteOnlyFile file,long fileSize){
        this.uuid = uuid;
        this.file = file;
        this.fileSize = fileSize;
    }

    public void start(){
        onCreate();
    }

    protected void showInfo(String info){}
    protected void setTransferProgress(double progress){}

    public void end(){
        file.close();
        if(file.length()==this.fileSize){
            this.onEndSuccess();
        }else{
            this.onEndFail();
            try{
                file.getFileObject().delete();
            }catch (FileOccupiedException e){
                Logger.getGlobal().log(Level.WARNING,"File delete failed.",e);
            }
        }
        onDelete();
    }

    public void end(String reason){
        showInfo(reason);
    }

    public void onDataReceived(byte[] data) throws IOException {
        file.write(data);
        transferredSize += data.length;
        setTransferProgress((double)transferredSize/fileSize);
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

}
