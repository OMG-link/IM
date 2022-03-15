package im.protocol.data_pack.user_list;

import im.config.Config;
import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;

public class SetRoomNamePack extends DataPack {
    private String roomName;

    public SetRoomNamePack(){
        super(DataPackType.SetRoomName);
        roomName = Config.getRoomName();
    }

    public SetRoomNamePack(ByteData data) throws InvalidPackageException {
        super(DataPackType.SetRoomName);
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
