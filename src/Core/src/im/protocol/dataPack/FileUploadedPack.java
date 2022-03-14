package im.protocol.dataPack;

import im.protocol.helper.data.ByteData;
import im.protocol.helper.data.InvalidPackageException;

import java.util.UUID;

public class FileUploadedPack extends DataPack{
    private String sender;
    private long stamp;
    private UUID fileId;
    private String fileName;
    private long fileSize;

    public FileUploadedPack(String sender, UUID fileId, String fileName, long fileSize){
        super(DataPackType.FileUploaded);
        this.sender = sender;
        this.stamp = System.currentTimeMillis();
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public FileUploadedPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.FileUploaded);
        this.decode(data);
    }

    @Override
    public ByteData encode(){
        ByteData data = new ByteData();
        data.append(super.encode());
        data.append(new ByteData(sender));
        data.append(new ByteData(stamp));
        data.append(new ByteData(fileId));
        data.append(new ByteData(fileName));
        data.append(new ByteData(fileSize));
        return data;
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
