package com.omg_link.im.protocol.data_pack.user_list;

import com.omg_link.im.protocol.data.ByteData;
import com.omg_link.im.protocol.data.InvalidPackageException;
import com.omg_link.im.protocol.data_pack.DataPack;
import com.omg_link.im.user_manager.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class BroadcastUserListPack extends DataPack {
    private final Collection<User> userList;

    public BroadcastUserListPack(Collection<User> userList){
        super(Type.BroadcastUserList);
        this.userList = userList;
    }

    public BroadcastUserListPack(ByteData data) throws InvalidPackageException {
        super(data);
        int length = data.decodeInt();
        this.userList = new ArrayList<>();
        for(int i=0;i<length;i++){
            this.userList.add(new User(data));
        }
    }

    @Override
    public ByteData encode(){
        ByteData data = super.encode();
        data.append(userList.size());
        for (User user : userList) {
            data.append(user);
        }
        return data;
    }

    public Collection<User> getUserList() {
        return Collections.unmodifiableCollection(userList);
    }

}
