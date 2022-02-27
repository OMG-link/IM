package protocol.dataPack;

import protocol.helper.data.Data;
import protocol.helper.data.InvalidPackageException;

public class DownloadReplyPack extends DataPack{
    private boolean ok;
    private String reason;

    public DownloadReplyPack(boolean ok,String reason){
        super(DataPackType.FileDownloadReply);
        this.ok = ok;
        this.reason = reason;
    }

    public DownloadReplyPack(Data data) throws InvalidPackageException {
        super(DataPackType.FileDownloadReply);
        this.decode(data);
    }

    @Override
    public Data encode(){
        Data data = new Data();
        data.append(super.encode());
        data.append(new Data(ok));
        data.append(new Data(reason));
        return data;
    }

    @Override
    public void decode(Data data) throws InvalidPackageException {
        super.decode(data);
        this.ok = Data.decodeBoolean(data);
        this.reason = Data.decodeString(data);
    }

    public boolean isOk() {
        return ok;
    }

    public String getReason() {
        return reason;
    }

}
