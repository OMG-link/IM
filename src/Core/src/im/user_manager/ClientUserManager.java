package im.user_manager;

import im.Client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ClientUserManager extends UserManager {
    private final Client client;
    private final Map<UUID,User> userList = new HashMap<>();
    private final CurrentUser currentUser = new CurrentUser(this);

    public ClientUserManager(Client client){
        this.client = client;
    }

    public void onUserListUpdated(){
        client.getRoomFrame().onUserListUpdate(getUserList());
    }

    public void joinUser(User user){
        if(userList.containsKey(user.getUid())){
            client.getLogger().log(
                    Level.WARNING,
                    "UUID conflict in ClientUserManager::onUserJoin. The old one will be replaced."
            );
        }
        user.setUserManager(this);
        userList.put(user.getUid(),user);
        onUserListUpdated();
    }

    public void removeUser(UUID uid){
        if(!userList.containsKey(uid)){
            client.getLogger().log(
                    Level.WARNING,
                    "UUID not found in ClientUserManager::onUserLeft. The operation will be ignored."
            );
            return;
        }
        userList.remove(uid);
        onUserListUpdated();
    }

    public void changeUsername(UUID uid, String name){
        if(!userList.containsKey(uid)){
            client.getLogger().log(
                    Level.WARNING,
                    "UUID not found in ClientUserManager::onUserNameChanged. The operation will be ignored."
            );
            return;
        }
        userList.get(uid).setName(name);
        onUserListUpdated();
    }

    public void updateFromUserList(Collection<User> userList){
        this.userList.clear();
        for(User user:userList){
            this.userList.put(user.getUid(),user);
        }
        onUserListUpdated();
    }

    public void onUsernameChanged(User user){
        onUserListUpdated();
    }

    public Collection<User> getUserList(){
        return userList.values();
    }

    public CurrentUser getCurrentUser() {
        return currentUser;
    }

}
