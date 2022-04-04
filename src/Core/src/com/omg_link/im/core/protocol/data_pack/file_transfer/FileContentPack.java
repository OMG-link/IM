package com.omg_link.im.core.protocol.data_pack.file_transfer;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data_pack.DataPack;

import java.util.Arrays;
import java.util.UUID;

public class FileContentPack extends DataPack {
    public static final int packSize = 63*1024; //63KB (1kb left for other information)

    private final UUID receiverTaskId;
    private final long offset;
    private final byte[] data;

    public FileContentPack(UUID receiverTaskId,long offset,byte[] data,int length){
        super(Type.FileContent);
        this.offset = offset;
        this.receiverTaskId = receiverTaskId;
        this.data = Arrays.copyOf(data,length);
    }

    public FileContentPack(ByteData data) throws InvalidPackageException {
        super(data);
        assert(getType()==Type.FileContent);
        this.offset = data.decodeLong();
        this.receiverTaskId = data.decodeUuid();
        this.data = data.decodeByteArray();
    }

    @Override
    public ByteData encode(){
        return super.encode()
                .append(offset)
                .append(receiverTaskId)
                .appendByteArray(data);
    }

    public long getOffset() {
        return offset;
    }

    public UUID getReceiverTaskId() {
        return receiverTaskId;
    }

    public byte[] getData() {
        return data;
    }

}
