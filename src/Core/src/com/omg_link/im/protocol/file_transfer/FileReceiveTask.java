package com.omg_link.im.protocol.file_transfer;

import com.omg_link.im.file_manager.*;
import com.omg_link.im.protocol.data.PackageTooLargeException;
import com.omg_link.im.protocol.data_pack.DataPack;
import com.omg_link.im.protocol.data_pack.file_transfer.FileTransferType;
import com.omg_link.im.protocol.data_pack.file_transfer.UploadResultPack;
import com.omg_link.mutils.FileUtils;
import com.omg_link.mutils.IllegalFileNameException;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class FileReceiveTask {
    private final FileTransferType fileTransferType;
    private UUID senderTaskId;
    private final UUID receiverTaskId;
    private UUID senderFileId;
    private UUID receiverFileId;

    private final String fileName;
    private FileObject fileObject;
    private Long fileSize;

    private WriteOnlyFile fileWriter;
    private long transferredSize = 0;

    //constructors

    protected FileReceiveTask(String fileName, FileTransferType fileTransferType, UUID receiverTaskId) throws IllegalFileNameException {
        this.fileName = fileName;
        this.fileTransferType = fileTransferType;
        this.receiverTaskId = receiverTaskId;
        if(FileUtils.isFileNameLegal(this.fileName)){
            throw new IllegalFileNameException();
        }
    }

    //abstract

    abstract void send(DataPack dataPack) throws IOException, PackageTooLargeException;

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

    public void onEndSucceed() {
        //close file
        if (fileWriter != null) {
            getFileWriter().close();
        }
        //reply to sender
        if (senderTaskId != null) {
            try {
                send(new UploadResultPack(
                        this,
                        true,
                        ""
                ));
            } catch (IOException ignored) {
            } catch (PackageTooLargeException e) {
                throw new RuntimeException(e);
            }
        }
        //task end
        this.removeFromFactory();
    }

    public void onEndFailed(String reason) {
        //close file
        if (fileWriter != null) {
            try {
                getFileWriter().close();
                getFileManager().deleteFile(receiverFileId);
            } catch (FileOccupiedException e) {
                Logger.getLogger("IMCore").log(
                        Level.WARNING,
                        String.format("File %s is occupied and cannot be deleted.", getFileWriter().getFileObject().getFile().getAbsoluteFile()),
                        e
                );
            } catch (NoSuchFileIdException ignored){}
        }
        //reply to sender
        if (senderTaskId != null) {
            try {
                send(new UploadResultPack(
                        this,
                        false,
                        reason
                ));
            } catch (IOException ignored) {
            } catch (PackageTooLargeException e) {
                throw new RuntimeException(e);
            }
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
            if (transferredSize == this.getFileSize()) {
                this.onEndSucceed();
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
        if(fileSize==null){
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
