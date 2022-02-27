package protocol.dataPack;

import protocol.helper.data.Data;
import protocol.helper.data.InvalidPackageException;

import java.util.UUID;

public class FileUploadedPack extends DataPack{
    private String sender;
    private long stamp;
    private UUID uuid;
    private String fileName;
    private long fileSize;

    public FileUploadedPack(String sender, UUID uuid, String fileName, long fileSize){
        super(DataPackType.FileUploaded);
        this.sender = sender;
        this.stamp = System.currentTimeMillis();
        this.uuid = uuid;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public FileUploadedPack(Data data) throws InvalidPackageException {
        super(DataPackType.FileUploaded);
        this.decode(data);
    }

    @Override
    public Data encode(){
        Data data = new Data();
        data.append(super.encode());
        data.append(new Data(sender));
        data.append(new Data(stamp));
        data.append(new Data(uuid));
        data.append(new Data(fileName));
        data.append(new Data(fileSize));
        return data;
    }

    @Override
    public void decode(Data data) throws InvalidPackageException {
        super.decode(data);
        sender = Data.decodeString(data);
        stamp = Data.decodeLong(data);
        uuid = Data.decodeUuid(data);
        fileName = Data.decodeString(data);
        fileSize = Data.decodeLong(data);
    }

    public String getSender() {
        return sender;
    }

    public long getStamp() {
        return stamp;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

}
