package com.omg_link.im.core.protocol.data_pack.file_transfer;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data_pack.DataPack;

import java.util.UUID;

public class UploadReplyPack extends DataPack {

    public enum State {
        startUpload,
        fileAlreadyExists,
        errorFileTooLarge, errorTaskNotFound, errorRemoteIOError, errorIllegalFileName
    }

    private final UUID senderTaskId;
    private final UUID receiverTaskId;
    private final UUID senderFileId;
    private final UUID receiverFileId;
    private final State state;
    private final FileTransferType fileTransferType;

    /**
     * Constructor used by client.
     */
    public UploadReplyPack(UploadRequestPack requestPack, State state){
        super(Type.FileUploadReply);
        this.senderTaskId = requestPack.getSenderTaskId();
        this.receiverTaskId = requestPack.getReceiverTaskId();
        this.senderFileId = requestPack.getSenderFileId();
        this.receiverFileId = requestPack.getReceiverFileId();
        this.state = state;
        this.fileTransferType = requestPack.getFileTransferType();
    }

    /**
     * Constructor used by server.
     */
    public UploadReplyPack(UploadRequestPack requestPack, UUID receiverTaskId, UUID receiverFileId, State state){
        super(Type.FileUploadReply);
        this.senderTaskId = requestPack.getSenderTaskId();
        this.receiverTaskId = receiverTaskId;
        this.senderFileId = requestPack.getSenderFileId();
        this.receiverFileId = receiverFileId;
        this.state = state;
        this.fileTransferType = requestPack.getFileTransferType();
    }

    public UploadReplyPack(ByteData data) throws InvalidPackageException {
        super(data);
        assert(getType()==Type.FileUploadReply);
        this.senderTaskId = data.decodeUuid();
        this.receiverTaskId = data.decodeUuid();
        this.senderFileId = data.decodeUuid();
        this.receiverFileId = data.decodeUuid();
        this.state = data.decodeEnum(State.values());
        this.fileTransferType = data.decodeEnum(FileTransferType.values());
    }

    @Override
    public ByteData encode() {
        return super.encode()
                .append(senderTaskId)
                .append(receiverTaskId)
                .append(senderFileId)
                .append(receiverFileId)
                .append(state)
                .append(fileTransferType);
    }

    public UUID getSenderTaskId() {
        return senderTaskId;
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

    public State getState() {
        return state;
    }

    public FileTransferType getFileTransferType() {
        return fileTransferType;
    }
}
