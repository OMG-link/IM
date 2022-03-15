package im.protocol.fileTransfer;

import im.file_manager.*;
import im.protocol.data_pack.*;
import im.protocol.data.PackageTooLargeException;
import im.protocol.data_pack.file_transfer.FileContentPack;
import im.protocol.data_pack.file_transfer.FileTransferType;
import im.protocol.data_pack.file_transfer.UploadFinishPack;
import im.protocol.data_pack.file_transfer.UploadReplyPack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

public abstract class FileSendTask implements Runnable {
    private final FileTransferType fileTransferType;
    private UUID senderTaskId;
    private UUID receiverTaskId;
    private UUID senderFileId;
    private UUID receiverFileId;

    private FileObject fileObject;
    private String fileName;

    protected String localEndReason = null;

    //constructors

    public FileSendTask(FileTransferType fileTransferType){
        this.fileTransferType = fileTransferType;
    }

    //abstract

    abstract void send(DataPack dataPack) throws IOException, PackageTooLargeException;
    abstract FileManager getFileManager();
    abstract void removeFromFactory();

    @Override
    public void run() {
        try{
            sendDataLoop();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            this.onTransferEnd();
        }
    }

    //start

    protected void setSenderTaskId(UUID senderTaskId){
        this.senderTaskId = senderTaskId;
    }

    public void setFile(File file) throws FileNotFoundException {
        setFile(file,file.getName());
    }

    public void setFile(File file, String fileName) throws FileNotFoundException {
        FileObject fileObject = getFileManager().openFile(file);
        setFileObject(fileObject,fileName);
    }

    public void setFileObject(FileObject fileObject,String fileName){
        this.fileObject = fileObject;
        this.senderFileId = fileObject.getFileId();
        this.fileName = fileName;
    }

    public void setReceiverTaskId(UUID receiverTaskId){
        this.receiverTaskId = receiverTaskId;
    }

    public void setSenderFileId(UUID senderFileId) {
        this.senderFileId = senderFileId;
    }

    public void setReceiverFileId(UUID receiverFileId){
        this.receiverFileId = receiverFileId;
    }

    public void start(){
        try{
            this.sendUploadRequestPack();
        }catch (IOException e){
            this.onTransferEnd();
            this.onEndFailed("Unable to send upload request pack.");
        }
    }

    //functions

    protected void onTransferProgressChange(long uploadedSize){}

    //steps

    abstract void sendUploadRequestPack() throws IOException ;

    public void onReceiveUploadReply(UploadReplyPack replyPack){
        if(replyPack.isOk()){
            setReceiverTaskId(replyPack.getReceiverTaskId());
            setReceiverFileId(replyPack.getReceiverFileId());
            Thread thread = new Thread(this,String.format("File Upload Task(%s)", getSenderTaskId()));
            thread.start();
        }else{
            this.localEndReason = "Upload request was rejected: "+replyPack.getReason();
            this.onTransferEnd();
        }
    }

    private void sendDataLoop() throws IOException {
        try(ReadOnlyFile fileReader = getFileManager().openFile(getSenderFileId()).getReadOnlyInstance()){
            long offset = 0;
            byte[] buffer = new byte[FileContentPack.packSize];
            while(true){
                int length = fileReader.read(buffer);
                if(length==-1) break;
                FileContentPack pack = new FileContentPack(getReceiverTaskId(),offset,buffer,length);
                try{
                    this.send(pack);
                }catch (PackageTooLargeException e){
                    throw new RuntimeException(e);
                }
                offset += length;
                this.onTransferProgressChange(offset);
            }
        }catch (NoSuchFileIdException e){
            localEndReason = "File not found.";
        }catch (FileOccupiedException e){
            localEndReason = "The file has been occupied.";
        }
        //return to run and call onTransferEnd
    }

    protected void onTransferEnd(){
        try{
            this.sendUploadFinishPack();
        }catch (IOException e1){
            //do nothing
        }
    }

    private void sendUploadFinishPack() throws IOException {
        try{
            this.send(new UploadFinishPack(this));
        }catch (PackageTooLargeException e){
            throw new RuntimeException(e);
        }
    }

    public void onEndSucceed(){
        this.removeFromFactory();
    }
    public void onEndFailed(String reason){
        this.removeFromFactory();
    }

    //get

    public UUID getSenderTaskId() {
        if(senderTaskId==null){
            throw new NullPointerException();
        }
        return senderTaskId;
    }

    public UUID getReceiverTaskId() {
        if(receiverTaskId==null){
            throw new NullPointerException();
        }
        return receiverTaskId;
    }

    public UUID getSenderFileId() {
        if(senderFileId==null){
            throw new NullPointerException();
        }
        return senderFileId;
    }

    public UUID getReceiverFileId() {
        if(receiverFileId==null){
            throw new NullPointerException();
        }
        return receiverFileId;
    }

    public File getFile() {
        if(fileObject==null){
            throw new NullPointerException();
        }
        return fileObject.getFile();
    }

    public String getFileName() {
        if(fileName==null){
            throw new NullPointerException();
        }
        return fileName;
    }

    public FileTransferType getFileTransferType() {
        if(fileTransferType==null){
            throw new NullPointerException();
        }
        return fileTransferType;
    }

}
