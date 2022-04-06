package com.omg_link.im.core.protocol.data_pack.system;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data_pack.DataPack;
import com.omg_link.im.core.user_manager.User;

import java.util.UUID;

/**
 * Server -> Client
 * <p>
 * Change UID.
 */
public class ConnectResultPack extends DataPack {
    public enum RejectReason {
        InvalidToken
    }

    private final boolean isTokenAccepted;

    //accepted
    UUID uid;
    UUID serverId;
    long lastMessageSerialId;

    //rejected
    RejectReason rejectReason;

    /**
     * Called when client is rejected.
     */
    public ConnectResultPack(RejectReason rejectReason){
        super(Type.ConnectResult);
        this.isTokenAccepted = false;
        this.rejectReason = rejectReason;
    }

    /**
     * Called when client is accepted.
     */
    public ConnectResultPack(User user, UUID serverId,long lastMessageSerialId){
        super(Type.ConnectResult);
        this.isTokenAccepted = true;
        this.uid = user.getUid();
        this.serverId = serverId;
        this.lastMessageSerialId = lastMessageSerialId;
    }

    public ConnectResultPack(ByteData data) throws InvalidPackageException {
        super(data);
        this.isTokenAccepted = data.decodeBoolean();
        if(isTokenAccepted()){
            this.uid = data.decodeUuid();
            this.serverId = data.decodeUuid();
            this.lastMessageSerialId = data.decodeLong();
        }else{
            this.rejectReason = data.decodeEnum(RejectReason.values());
        }
    }

    @Override
    public ByteData encode() {
        var data = super.encode()
                .append(isTokenAccepted);
        if(isTokenAccepted()){
            data.append(uid)
                    .append(serverId)
                    .append(lastMessageSerialId);
        }else{
            data.append(rejectReason);
        }
        return data;
    }

    public boolean isTokenAccepted() {
        return isTokenAccepted;
    }

    public UUID getUid() {
        return uid;
    }

    public UUID getServerId() {
        return serverId;
    }

    public long getLastMessageSerialId() {
        return lastMessageSerialId;
    }

    public RejectReason getRejectReason() {
        return rejectReason;
    }

}
