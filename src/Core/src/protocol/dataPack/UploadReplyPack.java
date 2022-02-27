package protocol.dataPack;

import protocol.helper.data.Data;
import protocol.helper.data.InvalidPackageException;

import java.util.UUID;

public class UploadReplyPack extends DataPack {
    private UUID uuid;
    private boolean ok;
    private String desc;

    public UploadReplyPack(Data data) throws InvalidPackageException {
        super(DataPackType.FileUploadReply);
        this.decode(data);
    }

    public UploadReplyPack(UUID uuid, boolean ok, String desc){
        super(DataPackType.FileUploadReply);
        this.uuid = uuid;
        this.ok = ok;
        this.desc = desc;
    }

    @Override
    public Data encode() {
        Data data = new Data();
        data.append(super.encode());
        data.append(new Data(uuid));
        data.append(new Data(ok));
        data.append(new Data(desc));
        return data;
    }

    @Override
    public void decode(Data data) throws InvalidPackageException {
        super.decode(data);
        this.uuid = Data.decodeUuid(data);
        this.ok = Data.decodeBoolean(data);
        this.desc = Data.decodeString(data);
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isOk() {
        return ok;
    }

    public String getDesc() {
        return desc;
    }

}
