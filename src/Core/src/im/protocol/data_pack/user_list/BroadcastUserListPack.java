package im.protocol.data_pack.user_list;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;
import im.user_manager.User;

import java.util.Collection;
import java.util.List;

public class BroadcastUserListPack extends DataPack {
    private User[] userList;

    public BroadcastUserListPack(Collection<User> userList){
        super(DataPackType.BroadcastUserList);
        this.userList = userList.toArray(new User[0]);
    }

    public BroadcastUserListPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.BroadcastUserList);
        this.decode(data);
    }

    @Override
    public ByteData encode(){
        ByteData data = super.encode();
        data.append(ByteData.encode(userList.length));
        for (User user : userList) {
            data.append(user.encodeToBytes());
        }
        return data;
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);

        int length = ByteData.decodeInt(data);
        userList = new User[length];
        for(int i=0;i<userList.length;i++){
            userList[i] = new User(data);
        }
    }

    public Collection<User> getUserList() {
        return List.of(userList);
    }

}
