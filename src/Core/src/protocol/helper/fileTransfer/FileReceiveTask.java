package protocol.helper.fileTransfer;

import mutil.FileUtil;
import mutil.file.FileManager;
import mutil.file.FileOccupiedException;
import mutil.file.WriteOnlyFile;
import mutil.uuidLocator.IUuidLocatable;
import mutil.uuidLocator.UuidConflictException;
import protocol.dataPack.DataPack;
import protocol.dataPack.FileTransferType;
import protocol.dataPack.UploadResultPack;
import protocol.helper.data.PackageTooLargeException;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class FileReceiveTask implements IUuidLocatable {
    private final FileTransferType fileTransferType;
    private UUID senderTaskId;
    private UUID receiverTaskId;
    private UUID senderFileId;
    private UUID receiverFileId;

    private File file;
    private Long fileSize;

    private WriteOnlyFile fileWriter;
    private long transferredSize = 0;

    //constructors

    protected FileReceiveTask(FileTransferType fileTransferType) {
        this.fileTransferType = fileTransferType;
    }

    //abstract

    abstract void send(DataPack dataPack) throws IOException, PackageTooLargeException;

    abstract FileManager getFileManager();

    @Override
    public UUID getUuid() {
        return getReceiverTaskId();
    }

    //start

    /**
     * Gives this task a unique receiverTaskId.
     */
    public void setReceiverTaskId() {
        while (true) {
            try {
                receiverTaskId = UUID.randomUUID();
                onCreate();
                break;
            } catch (UuidConflictException ignored) {
            }
        }
    }

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
        this.file = fileWriter.getFileObject().getFile();
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
        onDelete();
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
            getFileWriter().close();
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
        onDelete();
    }

    /**
     * End a task.
     * This calls onEndSucceed or on onEndFailed automatically.
     */
    public void end() {
        if (getFileWriter() != null) {
            if (getFileWriter().fileLength() == this.getFileSize()) {
                this.onEndSucceed();
            } else {
                this.onEndFailed(String.format(
                        "Actual received size does not match the request.(%s/%s)",
                        FileUtil.sizeToString(getFileWriter().fileLength()),
                        FileUtil.sizeToString(getFileSize()))
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

    public File getFile() {
        if (file == null) {
            throw new NullPointerException();
        }
        return file;
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

}
