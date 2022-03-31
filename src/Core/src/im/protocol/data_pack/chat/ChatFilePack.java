package im.protocol.data_pack.chat;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;

import java.util.UUID;

public class ChatFilePack extends DataPack {
    private String sender;
    private long stamp;
    private UUID fileId;
    private String fileName;
    private long fileSize;

    public ChatFilePack(String sender, UUID fileId, String fileName, long fileSize){
        super(DataPackType.FileUploaded);
        this.sender = sender;
        this.stamp = System.currentTimeMillis();
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public ChatFilePack(ByteData data) throws InvalidPackageException {
        super(DataPackType.FileUploaded);
        this.decode(data);
    }

    @Override
    public ByteData encode(){
        return super.encode()
                .append(ByteData.encode(sender))
                .append(ByteData.encode(stamp))
                .append(ByteData.encode(fileId))
                .append(ByteData.encode(fileName))
                .append(ByteData.encode(fileSize));
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        sender = ByteData.decodeString(data);
        stamp = ByteData.decodeLong(data);
        fileId = ByteData.decodeUuid(data);
        fileName = ByteData.decodeString(data);
        fileSize = ByteData.decodeLong(data);
    }

    public String getSender() {
        return sender;
    }

    public long getStamp() {
        return stamp;
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
