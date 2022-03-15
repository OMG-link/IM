package im.user_manager;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;

import java.util.UUID;

public class User {
    protected String name;
    protected UUID uid;
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

    public UUID getUid() {
        return uid;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public ByteData encodeToBytes(){
        return new ByteData()
                .append(ByteData.encode(name))
                .append(ByteData.encode(uid));
    }

    public void decodeFromBytes(ByteData data) throws InvalidPackageException {
        name = data.decodeString();
        uid = data.decodeUuid();
    }

}
