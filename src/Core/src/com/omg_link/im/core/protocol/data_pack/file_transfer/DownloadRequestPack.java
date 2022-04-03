package com.omg_link.im.core.protocol.data_pack.file_transfer;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data_pack.DataPack;

import java.util.UUID;

public class DownloadRequestPack extends DataPack {
    private final UUID receiverTaskId,senderFileId,receiverFileId;
    private final FileTransferType fileTransferType;

    public DownloadRequestPack(UUID receiverTaskId,UUID senderFileId, UUID receiverFileId, FileTransferType fileTransferType){
        super(Type.FileDownloadRequest);
        this.receiverTaskId = receiverTaskId;
        this.senderFileId = senderFileId;
        this.receiverFileId = receiverFileId;
        this.fileTransferType = fileTransferType;
    }

    public DownloadRequestPack(ByteData data) throws InvalidPackageException {
        super(data);
        assert(getType()==Type.FileDownloadRequest);
        this.receiverTaskId = data.decodeUuid();
        this.senderFileId = data.decodeUuid();
        this.receiverFileId = data.decodeUuid();
        this.fileTransferType = data.decodeEnum(FileTransferType.values());
    }

    @Override
    public ByteData encode(){
        return super.encode()
                .append(receiverTaskId)
                .append(senderFileId)
                .append(receiverFileId)
                .append(fileTransferType);
    }

    public UUID getReceiverTaskId() {
        return receiverTaskId;
    }

    public UUID getSenderFileId() {
        return senderFileId;
    }

    public UUID getReceiverFileId() {
        return receiverFileId;
    }

    public FileTransferType getFileTransferType() {
        return fileTransferType;
    }

}
