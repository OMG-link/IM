package protocol.dataPack;

import protocol.helper.data.Data;
import protocol.helper.data.InvalidPackageException;

import java.util.Arrays;

public class UserListPack extends DataPack {
    private String[] userList;

    public UserListPack(String[] userList){
        super(DataPackType.UserList);
        this.userList = Arrays.copyOf(userList,userList.length);
    }

    public UserListPack(Data data) throws InvalidPackageException {
        super(DataPackType.UserList);
        this.decode(data);
    }

    @Override
    public Data encode(){
        Data data = new Data();
        data.append(super.encode());
        data.append(new Data(userList.length));
        for(int i=0;i<userList.length;i++){
            data.append(new Data(userList[i]));
        }
        return data;
    }

    @Override
    public void decode(Data data) throws InvalidPackageException {
        super.decode(data);

        int length = Data.decodeInt(data);
        userList = new String[length];
        for(int i=0;i<userList.length;i++){
            userList[i] = Data.decodeString(data);
        }

    }

    public String[] getUserList() {
        return userList;
    }

}
