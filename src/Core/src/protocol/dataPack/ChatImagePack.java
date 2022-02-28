package protocol.dataPack;

import IM.Config;
import protocol.helper.data.ByteData;
import protocol.helper.data.InvalidPackageException;

import java.util.UUID;

public class ChatImagePack extends DataPack {
    private String sender;
    private long stamp;
    private UUID imageUUID;
    private ImageType imageType;

    public ChatImagePack(UUID imageUUID, ImageType imageType) {
        super(DataPackType.ChatImage);
        this.sender = Config.getUsername();
        this.stamp = System.currentTimeMillis();
        this.imageUUID = imageUUID;
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
                .append(ByteData.encode(imageUUID))
                .append(ByteData.encode(imageType.toId()));
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        this.sender = data.decodeString();
        this.stamp = data.decodeLong();
        this.imageUUID = data.decodeUuid();
        this.imageType = ImageType.toType(data.decodeInt());
    }

    public String getSender() {
        return sender;
    }

    public long getStamp() {
        return stamp;
    }

    public UUID getImageUUID() {
        return imageUUID;
    }

    public ImageType getImageType() {
        return imageType;
    }

}
