package im.protocol.dataPack;

import im.config.Config;
import im.protocol.helper.data.ByteData;
import im.protocol.helper.data.InvalidPackageException;

public class RoomNamePack extends DataPack {
    private String roomName;

    public RoomNamePack(){
        super(DataPackType.RoomName);
        roomName = Config.getRoomName();
    }

    public RoomNamePack(ByteData data) throws InvalidPackageException {
        super(DataPackType.RoomName);
        this.decode(data);
    }

    @Override
    public ByteData encode(){
        return new ByteData()
                .append(super.encode())
                .append(ByteData.encode(roomName));
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException  {
        super.decode(data);
        roomName = data.decodeString();
    }

    public String getRoomName() {
        return roomName;
    }

}
