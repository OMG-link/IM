package protocol.dataPack;

import IM.Config;
import protocol.helper.data.Data;
import protocol.helper.data.InvalidPackageException;

public class TextPack extends DataPack {
    private String sender;
    private long stamp;
    private String text;

    public TextPack(String text){
        super(DataPackType.Text);
        this.sender = Config.getUsername();
        this.stamp = System.currentTimeMillis();
        this.text = text;
    }

    public TextPack(Data data) throws InvalidPackageException {
        super(DataPackType.Text);
        this.decode(data);
    }

    @Override
    public Data encode(){
        Data data = new Data();
        data.append(super.encode());
        data.append(new Data(sender));
        data.append(new Data(stamp));
        data.append(Data.encodeString(text));
        return data;
    }

    @Override
    public void decode(Data data) throws InvalidPackageException {
        super.decode(data);
        sender = Data.decodeString(data);
        stamp = Data.decodeLong(data);
        text = Data.decodeString(data);
    }

    public String getText() {
        return text;
    }

    public String getSender() {
        return sender;
    }

    public long getStamp() {
        return stamp;
    }

    public void setStamp(long stamp) {
        this.stamp = stamp;
    }

    public void setStamp(){
        this.setStamp(System.currentTimeMillis());
    }

}
