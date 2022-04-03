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
    private RejectReason rejectReason;
    private UUID uid;

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
    public ConnectResultPack(User user){
        super(Type.ConnectResult);
        this.isTokenAccepted = true;
        this.uid = user.getUid();
    }

    public ConnectResultPack(ByteData data) throws InvalidPackageException {
        super(data);
        this.isTokenAccepted = data.decodeBoolean();
        if(isTokenAccepted()){
            this.uid = data.decodeUuid();
        }else{
            this.rejectReason = data.decodeEnum(RejectReason.values());
        }
    }

    @Override
    public ByteData encode() {
        var data = super.encode()
                .append(isTokenAccepted);
        if(isTokenAccepted()){
            data.append(uid);
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

    public RejectReason getRejectReason() {
        return rejectReason;
    }

}
