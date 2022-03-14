package im.protocol.dataPack;

import im.protocol.helper.data.ByteData;
import im.protocol.helper.data.InvalidPackageException;

import java.util.Arrays;

public class UserListPack extends DataPack {
    private String[] userList;

    public UserListPack(String[] userList){
        super(DataPackType.UserList);
        this.userList = Arrays.copyOf(userList,userList.length);
    }

    public UserListPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.UserList);
        this.decode(data);
    }

    @Override
    public ByteData encode(){
        ByteData data = new ByteData();
        data.append(super.encode());
        data.append(new ByteData(userList.length));
        for (String s : userList) {
            data.append(new ByteData(s));
        }
        return data;
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);

        int length = ByteData.decodeInt(data);
        userList = new String[length];
        for(int i=0;i<userList.length;i++){
            userList[i] = ByteData.decodeString(data);
        }

    }

    public String[] getUserList() {
        return userList;
    }

}
