package com.omg_link.im.user_manager;

import com.omg_link.im.config.Config;
import com.omg_link.im.config.InvalidUserNameException;

import java.util.UUID;

public class CurrentUser extends User{
    private final ClientUserManager clientUserManager;

    public CurrentUser(ClientUserManager clientUserManager){
        super(Config.getUsername());
        this.clientUserManager = clientUserManager;
    }

    public void setNameByInput(String name) throws InvalidUserNameException {
        Config.setUsername(name);
        super.setName(name);
    }

    public void setUid(UUID uid){
        this.uid = uid;
        clientUserManager.joinUser(this);
    }

}
