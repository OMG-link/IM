package im.user_manager;

import im.Server;
import im.protocol.data_pack.user_list.BroadcastUserListPack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerUserManager {
    private final Server server;
    private final Map<UUID,User> map = new HashMap<>();

    public ServerUserManager(Server server){
        this.server = server;
    }

    public void onUserListUpdated(){
        server.getNetworkHandler().broadcast(new BroadcastUserListPack(getUserList()),false);
    }

    public User createUser(){
        UUID uuid;
        do{
            uuid = UUID.randomUUID();
        }while (map.containsKey(uuid));
        User user = new User(uuid);
        map.put(uuid,user);
        onUserListUpdated();
        return user;
    }

    public void removeUser(User user) throws NoSuchUidException {
        if(!map.containsKey(user.getUid())){
            throw new NoSuchUidException();
        }
        map.remove(user.getUid());
        onUserListUpdated();
    }

    public Collection<User> getUserList(){
        return map.values();
    }

}
