package protocol.dataPack;

import protocol.helper.data.Data;
import protocol.helper.data.InvalidPackageException;

import java.util.UUID;

public class UploadRequestPack extends DataPack {
    private UUID uuid;
    private String fileName;
    private long fileSize;

    public UploadRequestPack(Data data) throws InvalidPackageException {
        super(DataPackType.FileUploadRequest);
        this.decode(data);
    }

    public UploadRequestPack(String fileName,long fileSize, UUID uuid){
        super(DataPackType.FileUploadRequest);
        this.uuid = uuid;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    @Override
    public Data encode(){
        Data data = new Data();
        data.append(super.encode());
        data.append(new Data(this.uuid));
        data.append(new Data(this.fileName));
        data.append(new Data(this.fileSize));
        return data;
    }

    @Override
    public void decode(Data data) throws InvalidPackageException {
        super.decode(data);
        this.uuid = Data.decodeUuid(data);
        this.fileName = Data.decodeString(data);
        this.fileSize = Data.decodeLong(data);
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public UUID getUuid() {
        return uuid;
    }

}
