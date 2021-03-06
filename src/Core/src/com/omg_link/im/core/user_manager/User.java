package com.omg_link.im.core.user_manager;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.IEncodeable;
import com.omg_link.im.core.protocol.data.InvalidPackageException;

import java.util.UUID;

public class User implements IEncodeable {
    protected String name;
    protected UUID uid;
    protected UUID avatarFileId = new UUID(0,0);
    private UserManager userManager = null;

    /**
     * Constructor for CurrentUser.
     */
    protected User(String name){
        this(name,new UUID(0,0));
    }

    public User(String name,UUID uid){
        this.name = name;
        this.uid = uid;
    }

    public User(ByteData data) throws InvalidPackageException {
        this.name = data.decodeString();
        this.uid = data.decodeUuid();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if(userManager!=null){
            userManager.onUsernameChanged(this);
        }
    }

    public UUID getAvatarFileId() {
        return avatarFileId;
    }

    public void setAvatarFileId(UUID avatarFileId) {
        this.avatarFileId = avatarFileId;
    }

    public UUID getUid() {
        return uid;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public ByteData encode(){
        return new ByteData()
                .append(ByteData.encode(name))
                .append(ByteData.encode(uid));
    }

}
