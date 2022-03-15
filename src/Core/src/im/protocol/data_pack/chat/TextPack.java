package im.protocol.data_pack.chat;

import im.config.Config;
import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;

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
        return super.encode()
                .append(ByteData.encode(sender))
                .append(ByteData.encode(stamp))
                .append(ByteData.encode(text));
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
