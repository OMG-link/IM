package com.omg_link.im.core.user_manager;

import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.config.ConfigSetFailedException;

import java.util.UUID;

public class CurrentUser extends User{
    private final ClientUserManager clientUserManager;

    public CurrentUser(ClientUserManager clientUserManager){
        super(Config.getUsername());
        this.clientUserManager = clientUserManager;
    }

    public void setNameByInput(String name) throws ConfigSetFailedException {
        Config.setUsername(name);
        super.setName(name);
    }

    public void setUid(UUID uid){
        this.uid = uid;
        clientUserManager.joinUser(this);
    }

}
