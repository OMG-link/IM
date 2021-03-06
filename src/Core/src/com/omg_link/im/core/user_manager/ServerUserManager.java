package com.omg_link.im.core.user_manager;

import com.omg_link.im.core.ServerRoom;
import com.omg_link.im.core.protocol.data_pack.user_list.BroadcastUserJoinPack;
import com.omg_link.im.core.protocol.data_pack.user_list.BroadcastUserLeftPack;
import com.omg_link.im.core.protocol.data_pack.user_list.BroadcastUsernameChangedPack;
import com.omg_link.im.core.protocol.data_pack.DataPack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerUserManager extends UserManager {
    private final ServerRoom serverRoom;
    private final Map<UUID,User> map = new HashMap<>();

    public ServerUserManager(ServerRoom serverRoom){
        this.serverRoom = serverRoom;
    }

    public User createUser(String username){
        UUID uuid;
        do{
            uuid = UUID.randomUUID();
        }while (map.containsKey(uuid));
        User user = new User(username,uuid);
        user.setUserManager(this);
        map.put(uuid,user);
        onUserJoin(user);
        return user;
    }

    public void removeUser(User user) throws NoSuchUidException {
        if(!map.containsKey(user.getUid())){
            throw new NoSuchUidException();
        }
        map.remove(user.getUid());
        onUserLeft(user);
    }

    public void broadcast(DataPack dataPack){
        serverRoom.getNetworkHandler().broadcast(dataPack);
    }

    private void onUserJoin(User user){
        broadcast(new BroadcastUserJoinPack(user));
    }

    private void onUserLeft(User user){
        broadcast(new BroadcastUserLeftPack(user));
    }

    public void onUsernameChanged(User user){
        broadcast(new BroadcastUsernameChangedPack(user));
    }

    public Collection<User> getUserList(){
        return map.values();
    }

}
