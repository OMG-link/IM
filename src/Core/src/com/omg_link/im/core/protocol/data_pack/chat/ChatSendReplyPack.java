package com.omg_link.im.core.protocol.data_pack.chat;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data_pack.DataPack;

import java.util.UUID;

public class ChatSendReplyPack extends DataPack {

    public enum Reason {
        ChatTextTooLong
    }

    private final boolean ok;
    private UUID msgId;
    private long serialId;
    private Reason reason;

    /**
     * Called when send is accepted.
     */
    public ChatSendReplyPack(UUID msgId, long serialId) {
        super(Type.ChatSendReply);
        this.ok = true;
        this.msgId = msgId;
        this.serialId = serialId;
    }

    /**
     * Called when send is rejected.
     */
    public ChatSendReplyPack(Reason reason){
        super(Type.ChatSendReply);
        this.ok = false;
        this.reason = reason;
    }

    public ChatSendReplyPack(ByteData data) throws InvalidPackageException {
        super(data);
        this.ok = data.decodeBoolean();
        if (isOk()) {
            this.msgId = data.decodeUuid();
            this.serialId = data.decodeLong();
        } else {
            this.reason = data.decodeEnum(Reason.values());
        }
    }

    @Override
    public ByteData encode() {
        var result = super.encode()
                .append(ok);
        if (isOk()) {
            result.append(msgId)
                    .append(serialId);
        } else {
            result.append(reason);
        }
        return result;
    }

    public boolean isOk() {
        return ok;
    }

    public UUID getMsgId() {
        return msgId;
    }

    public long getSerialId() {
        return serialId;
    }

    public Reason getReason() {
        return reason;
    }

}
