package com.omg_link.im.protocol.data_pack.chat;

import com.omg_link.im.protocol.data.ByteData;
import com.omg_link.im.protocol.data.InvalidPackageException;
import com.omg_link.im.protocol.data_pack.DataPack;

import java.util.UUID;

class ChatFilePack extends DataPack {
    private final UUID fileId;
    private final String fileName;
    private final long fileSize;

    public ChatFilePack(UUID fileId, String fileName, long fileSize){
        super(Type.ChatFile);
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public ChatFilePack(ByteData data) throws InvalidPackageException {
        super(data);
        assert(getType()== Type.ChatFile);
        this.fileId = data.decodeUuid();
        this.fileName = data.decodeString();
        this.fileSize = data.decodeLong();
    }

    @Override
    public ByteData encode(){
        return super.encode()
                .append(fileId)
                .append(fileName)
                .append(fileSize);
    }

    public UUID getFileId() {
        return fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

}
