package protocol.dataPack;

import protocol.helper.data.ByteData;
import protocol.helper.data.InvalidPackageException;

import java.util.UUID;

public class DownloadReplyPack extends DataPack{
    private UUID receiverTaskId;
    private boolean ok;
    private String reason;
    private FileTransferType fileTransferType;

    public DownloadReplyPack(UUID receiverTaskId,boolean ok,String reason,FileTransferType fileTransferType){
        super(DataPackType.FileDownloadReply);
        this.receiverTaskId = receiverTaskId;
        this.ok = ok;
        this.reason = reason;
        this.fileTransferType = fileTransferType;
    }

    public DownloadReplyPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.FileDownloadReply);
        this.decode(data);
    }

    @Override
    public ByteData encode(){
        ByteData data = new ByteData();
        data.append(super.encode());
        data.append(ByteData.encode(receiverTaskId));
        data.append(new ByteData(ok));
        data.append(new ByteData(reason));
        data.append(new ByteData(fileTransferType.toId()));
        return data;
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        this.receiverTaskId = data.decodeUuid();
        this.ok = data.decodeBoolean();
        this.reason = data.decodeString();
        this.fileTransferType = FileTransferType.toType(data.decodeInt());
    }

    public UUID getReceiverTaskId() {
        return receiverTaskId;
    }

    public boolean isOk() {
        return ok;
    }

    public String getReason() {
        return reason;
    }

    public FileTransferType getFileTransferType() {
        return fileTransferType;
    }
}
