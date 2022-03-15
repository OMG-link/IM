package im.user_manager;

import im.Client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientUserManager {
    private final Client client;
    private final Map<UUID,User> userList = new HashMap<>();

    public ClientUserManager(Client client){
        this.client = client;
    }

    public void onUserListUpdated(){
        client.getRoomFrame().onUserListUpdate(getUserList());
    }

    public void updateFromUserList(Collection<User> userList){
        this.userList.clear();
        for(User user:userList){
            this.userList.put(user.getUid(),user);
        }
        onUserListUpdated();
    }

    public Collection<User> getUserList(){
        return userList.values();
    }

}
