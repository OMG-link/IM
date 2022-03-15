package im.user_manager;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;

import java.util.UUID;

public class User {
    private String name;
    private UUID uid;

    public User(UUID uid){
        this("anonymous",uid);
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
    }

    public UUID getUid() {
        return uid;
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
