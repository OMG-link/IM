package com.omg_link.im.core.user_manager;

import com.omg_link.im.core.ClientRoom;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ClientUserManager extends UserManager {
    private final ClientRoom room;
    private final Map<UUID,User> userList = new HashMap<>();
    private final CurrentUser currentUser = new CurrentUser(this);

    public ClientUserManager(ClientRoom room){
        this.room = room;
    }

    public void onUserListUpdated(){
        room.getRoomFrame().updateUserList(getUserList());
    }

    public void joinUser(User user){
        if(userList.containsKey(user.getUid())){
            room.getLogger().log(
                    Level.WARNING,
                    "UUID conflict in ClientUserManager::joinUser. The old one will be replaced."
            );
        }
        user.setUserManager(this);
        userList.put(user.getUid(),user);
        room.getRoomFrame().onUserJoined(user);
    }

    public void removeUser(UUID uid){
        if(!userList.containsKey(uid)){
            room.getLogger().log(
                    Level.WARNING,
                    "UUID not found in ClientUserManager::removeUser. The operation will be ignored."
            );
            return;
        }
        var user = userList.remove(uid);
        room.getRoomFrame().onUserLeft(user);
    }

    public void changeUsername(UUID uid, String name){
        if(!userList.containsKey(uid)){
            room.getLogger().log(
                    Level.WARNING,
                    "UUID not found in ClientUserManager::changeUsername. The operation will be ignored."
            );
            return;
        }
        var user = userList.get(uid);
        var previousName = user.getName();
        userList.get(uid).setName(name);
        room.getRoomFrame().onUsernameChanged(user,previousName);
    }

    public void updateFromUserList(Collection<User> userList){
        this.userList.clear();
        for(User user:userList){
            this.userList.put(user.getUid(),user);
        }
        this.userList.put(currentUser.getUid(),currentUser); //Replace the one from network.
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
