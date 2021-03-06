package com.omg_link.im.core.protocol.file_transfer;

import com.omg_link.im.core.file_manager.*;
import com.omg_link.im.core.protocol.data.PackageTooLargeException;
import com.omg_link.im.core.protocol.data_pack.DataPack;
import com.omg_link.im.core.protocol.data_pack.file_transfer.UploadResultPack;
import com.omg_link.utils.FileUtils;
import com.omg_link.utils.Sha512Digest;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class FileReceiveTask {
    final FileTransferType fileTransferType;
    UUID senderTaskId;
    final UUID receiverTaskId;
    UUID senderFileId;
    UUID receiverFileId;
    Sha512Digest senderSideDigest;

    final String fileName;
    FileObject fileObject;
    Long fileSize;

    WriteOnlyFile fileWriter;
    long transferredSize = 0;

    //constructors

    protected FileReceiveTask(String fileName, FileTransferType fileTransferType, UUID receiverTaskId) {
        this.fileName = fileName;
        this.fileTransferType = fileTransferType;
        this.receiverTaskId = receiverTaskId;
    }

    //abstract

    abstract void send(DataPack dataPack) throws PackageTooLargeException;

    abstract FileManager getFileManager();

    abstract void removeFromFactory();

    //start

    public void setSenderTaskId(UUID senderTaskId) {
        this.senderTaskId = senderTaskId;
    }

    public void setSenderFileId(UUID senderFileId) {
        this.senderFileId = senderFileId;
    }

    public void setReceiverFileId(UUID receiverFileId) {
        this.receiverFileId = receiverFileId;
    }

    public void setSenderSideDigest(Sha512Digest senderSideDigest) {
        this.senderSideDigest = senderSideDigest;
    }

    public void setFileWriter(WriteOnlyFile fileWriter) {
        this.fileWriter = fileWriter;
        this.fileObject = fileWriter.getFileObject();
        this.fileSize = fileWriter.fileLength();
    }

    /**
     * @throws IOException When the file is created failed.
     */
    public void setFileSize(Long fileSize) throws IOException {
        this.fileSize = fileSize;
        this.fileWriter.setLength(fileSize);
    }

    //end

    protected void onEndSucceed() {
        //reply to sender
        if (senderTaskId != null) {
            send(new UploadResultPack(
                    this,
                    true,
                    ""
            ));
        }
        //task end
        this.removeFromFactory();
    }

    public void onEndFailed(String reason) {
        //close file
        if (fileWriter != null) {
            try {
                getFileWriter().close();
                getFileWriter().getFileObject().delete();
            } catch (FileOccupiedException e) {
                Logger.getLogger("IMCore").log(
                        Level.WARNING,
                        String.format("File %s is occupied and cannot be deleted.", getFileWriter().getFileObject().getFile().getAbsoluteFile()),
                        e
                );
            }
        }
        //reply to sender
        if (senderTaskId != null) {
            send(new UploadResultPack(
                    this,
                    false,
                    reason
            ));
        }
        //task end
        this.removeFromFactory();
    }

    /**
     * End a task.
     * This calls onEndSucceed or on onEndFailed automatically.
     */
    public void end() {
        if (getFileWriter() != null) {
            getFileWriter().close();
            if (transferredSize == this.getFileSize()) {
                try{
                    Sha512Digest receiverSideDigest = getFileWriter().getFileObject().getSha512Digest();
                    if(!Objects.equals(receiverSideDigest,senderSideDigest)){
                        this.onEndFailed("Wrong digest.");
                    }else{
                        this.onEndSucceed();
                    }
                }catch (IOException e){
                    this.onEndFailed("Cannot calculate file digest.");
                }
            } else {
                this.onEndFailed(String.format(
                        "Actual received size does not match the request.(%s/%s)",
                        FileUtils.sizeToString(transferredSize),
                        FileUtils.sizeToString(getFileSize()))
                );
            }
        } else {
            this.onEndFailed("Nothing received.");
        }
    }

    // Transferring functions

    public void onDataReceived(byte[] data) throws IOException {
        getFileWriter().write(data);
        transferredSize = getTransferredSize() + data.length;
        onTransferProgressChange(getTransferredSize());
    }

    protected void onTransferProgressChange(long downloadedSize) {
    }

    // Getters

    public UUID getSenderTaskId() {
        if (senderTaskId == null) {
            throw new NullPointerException();
        }
        return senderTaskId;
    }

    public UUID getReceiverTaskId() {
        if (receiverTaskId == null) {
            throw new NullPointerException();
        }
        return receiverTaskId;
    }

    public UUID getSenderFileId() {
        if (senderFileId == null) {
            throw new NullPointerException();
        }
        return senderFileId;
    }

    public UUID getReceiverFileId() {
        if (receiverFileId == null) {
            throw new NullPointerException();
        }
        return receiverFileId;
    }

    public FileObject getFileObject() {
        if (fileObject == null) {
            throw new NullPointerException();
        }
        return fileObject;
    }

    public long getFileSize() {
        if (fileSize == null) {
            throw new NullPointerException();
        }
        return fileSize;
    }

    public FileTransferType getFileTransferType() {
        if (fileTransferType == null) {
            throw new NullPointerException();
        }
        return fileTransferType;
    }

    public WriteOnlyFile getFileWriter() {
        if (fileWriter == null) {
            throw new NullPointerException();
        }
        return fileWriter;
    }

    public long getTransferredSize() {
        return transferredSize;
    }

    public String getFileName() {
        return fileName;
    }

}
