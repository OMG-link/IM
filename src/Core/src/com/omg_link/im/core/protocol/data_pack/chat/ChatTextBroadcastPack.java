package com.omg_link.im.core.protocol.data_pack.chat;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.user_manager.User;

import java.util.UUID;

public class ChatTextBroadcastPack extends ChatTextPack{

    private final long serialId;
    private final String username;
    private final UUID userAvatarFileId;
    private final long stamp;

    public ChatTextBroadcastPack(long serialId, User user, String text){
        super(text);
        this.serialId = serialId;
        this.username = user.getName();
        this.userAvatarFileId = user.getAvatarFileId();
        this.stamp = System.currentTimeMillis();
    }

    public ChatTextBroadcastPack(ByteData data) throws InvalidPackageException {
        super(data);
        this.serialId = data.decodeLong();
        this.username = data.decodeString();
        this.userAvatarFileId = data.decodeUuid();
        this.stamp = data.decodeLong();
    }

    @Override
    public ByteData encode() {
        return super.encode()
                .append(serialId)
                .append(username)
                .append(userAvatarFileId)
                .append(stamp);
    }

    public long getSerialId() {
        return serialId;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUserAvatarFileId() {
        return userAvatarFileId;
    }

    public long getStamp() {
        return stamp;
    }

}
