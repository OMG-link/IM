package protocol.dataPack;

import IM.Config;
import protocol.helper.data.ByteData;
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

    public TextPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.Text);
        this.decode(data);
    }

    @Override
    public ByteData encode(){
        ByteData data = new ByteData();
        data.append(super.encode());
        data.append(new ByteData(sender));
        data.append(new ByteData(stamp));
        data.append(ByteData.encode(text));
        return data;
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        sender = ByteData.decodeString(data);
        stamp = ByteData.decodeLong(data);
        text = ByteData.decodeString(data);
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
