package im.protocol.dataPack;

import im.config.Config;
import im.protocol.helper.data.ByteData;
import im.protocol.helper.data.InvalidPackageException;

import java.util.UUID;

public class ChatImagePack extends DataPack {
    private String sender;
    private long stamp;
    private UUID serverImageId;
    private ImageType imageType;

    public ChatImagePack(UUID serverImageId, ImageType imageType) {
        super(DataPackType.ChatImage);
        this.sender = Config.getUsername();
        this.stamp = System.currentTimeMillis();
        this.serverImageId = serverImageId;
        this.imageType = imageType;
    }

    public ChatImagePack(ByteData data) throws InvalidPackageException {
        super(DataPackType.ChatImage);
        this.decode(data);
    }

    @Override
    public ByteData encode() {
        return new ByteData()
                .append(super.encode())
                .append(ByteData.encode(sender))
                .append(ByteData.encode(stamp))
                .append(ByteData.encode(serverImageId))
                .append(ByteData.encode(imageType.toId()));
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        this.sender = data.decodeString();
        this.stamp = data.decodeLong();
        this.serverImageId = data.decodeUuid();
        this.imageType = ImageType.toType(data.decodeInt());
    }

    public String getSender() {
        return sender;
    }

    public long getStamp() {
        return stamp;
    }

    public UUID getServerImageId() {
        return serverImageId;
    }

    public ImageType getImageType() {
        return imageType;
    }

}
