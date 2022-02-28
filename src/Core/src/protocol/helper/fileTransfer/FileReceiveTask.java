package protocol.helper.fileTransfer;

import mutil.file.FileOccupiedException;
import mutil.file.WriteOnlyFile;
import mutil.uuidLocator.IUuidLocatable;
import mutil.uuidLocator.UuidConflictException;
import protocol.dataPack.DataPack;
import protocol.dataPack.FileTransferType;
import protocol.dataPack.UploadResultPack;
import protocol.helper.data.PackageTooLargeException;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class FileReceiveTask implements IUuidLocatable {
    protected UUID senderTaskId, receiverTaskId;
    private long transferredSize = 0;

    protected WriteOnlyFile file;
    protected long fileSize;
    protected UUID localFileId;

    protected abstract FileTransferType getFileTransferType();
    protected abstract void send(DataPack dataPack) throws IOException, PackageTooLargeException;

    protected void onEndSucceed(){
        if(senderTaskId==null) return;
        try{
            send(new UploadResultPack(
                    senderTaskId,
                    file.getFileObject().getFileId(),
                    true,
                    ""
            ));
        }catch (IOException ignored){
        }catch (PackageTooLargeException e){
            throw new RuntimeException(e);
        }
    }

    protected void onEndFailed(String reason){
        if(senderTaskId==null) return;
        try{
            send(new UploadResultPack(
                    senderTaskId,
                    new UUID(0,0),
                    false,
                    reason
            ));
        }catch (IOException ignored){
        }catch (PackageTooLargeException e){
            throw new RuntimeException(e);
        }
    }

    public void init(){
        while(true){
            try{
                receiverTaskId = UUID.randomUUID();
                onCreate();
                break;
            }catch (UuidConflictException ignored){
            }
        }
    }

    protected void showInfo(String info){}
    protected void setTransferProgress(double progress){}

    public void end(){
        if(file!=null){
            file.close();
            if(file.length()==this.fileSize){
                this.onEndSucceed();
            }else{
                this.onEndFailed("Actual received size does not match the request.");
                try{
                    file.getFileObject().delete();
                }catch (FileOccupiedException e){
                    Logger.getLogger("IMCore").log(
                            Level.WARNING,
                            String.format("File %s is occupied and cannot be deleted.",file.getFileObject().getFile().getAbsoluteFile()),
                            e
                    );
                }
            }
        }else{
            this.onEndFailed("Nothing received.");
        }
        onDelete();
    }

    public void end(String reason){
        end();
        showInfo(reason);
    }

    public void onDataReceived(byte[] data) throws IOException {
        file.write(data);
        transferredSize += data.length;
        setTransferProgress((double)transferredSize/fileSize);
    }

    @Override
    public UUID getUuid() {
        return receiverTaskId;
    }

    public UUID getReceiverTaskId() {
        return receiverTaskId;
    }

    public void setWriteOnlyFile(WriteOnlyFile file) {
        this.localFileId = file.getFileObject().getFileId();
        this.file = file;
    }

    public void setFileSize(long size){
        this.fileSize = size;
    }

    public void setSenderTaskId(UUID senderTaskId) {
        this.senderTaskId = senderTaskId;
    }

    public UUID getLocalFileId() {
        return localFileId;
    }

}
