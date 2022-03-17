package im.protocol.data_pack.system;

import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data_pack.DataPack;
import im.protocol.data_pack.DataPackType;
import im.user_manager.User;

import java.security.InvalidParameterException;
import java.util.UUID;

/**
 * Server -> Client
 * <p>
 * Change UID.
 */
public class ConnectResultPack extends DataPack {
    public enum RejectReason{
        InvalidToken;

        public int toId() {
            return RejectReason.toId(this);
        }

        public static int toId(RejectReason type) {
            return type.ordinal();
        }

        public static RejectReason toType(int id) {
            if (id < 0 || id >= values().length) {
                throw new InvalidParameterException(String.format("Invalid data pack type %d.", id));
            }
            return values()[id];
        }

    }

    private boolean isTokenAccepted;
    private RejectReason rejectReason;
    private UUID uid;

    /**
     * Called when client is rejected.
     */
    public ConnectResultPack(RejectReason rejectReason){
        super(DataPackType.ConnectResult);
        this.isTokenAccepted = false;
        this.rejectReason = rejectReason;
    }

    /**
     * Called when client is accepted.
     */
    public ConnectResultPack(User user){
        super(DataPackType.ConnectResult);
        this.isTokenAccepted = true;
        this.uid = user.getUid();
    }

    public ConnectResultPack(ByteData data) throws InvalidPackageException {
        super(DataPackType.ConnectResult);
        decode(data);
    }

    @Override
    public ByteData encode() {
        if(isTokenAccepted()){
            return super.encode()
                    .append(ByteData.encode(isTokenAccepted))
                    .append(ByteData.encode(uid));
        }else{
            return super.encode()
                    .append(ByteData.encode(isTokenAccepted))
                    .append(ByteData.encode(rejectReason.toId()));
        }
    }

    @Override
    public void decode(ByteData data) throws InvalidPackageException {
        super.decode(data);
        this.isTokenAccepted = data.decodeBoolean();
        if(isTokenAccepted){
            this.uid = data.decodeUuid();
        }else{
            this.rejectReason = RejectReason.toType(data.decodeInt());
        }
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
