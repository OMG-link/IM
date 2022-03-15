package im.user_manager;

import im.config.Config;
import im.config.InvalidUserNameException;

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
