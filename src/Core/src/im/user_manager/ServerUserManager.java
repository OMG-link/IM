package im.user_manager;

import im.Server;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.user_list.BroadcastUserJoinPack;
import im.protocol.data_pack.user_list.BroadcastUserLeftPack;
import im.protocol.data_pack.user_list.BroadcastUsernameChangedPack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerUserManager extends UserManager {
    private final Server server;
    private final Map<UUID,User> map = new HashMap<>();

    public ServerUserManager(Server server){
        this.server = server;
    }

    public User createUser(){
        UUID uuid;
        do{
            uuid = UUID.randomUUID();
        }while (map.containsKey(uuid));
        User user = new User("anonymous",uuid);
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
        server.getNetworkHandler().broadcast(dataPack,false);
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
