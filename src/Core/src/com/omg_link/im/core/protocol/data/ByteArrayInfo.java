package com.omg_link.im.core.protocol.data;

public class ByteArrayInfo {
    private final byte[] array;
    private final int offset;
    private final int length;

    public ByteArrayInfo(byte[] array, int offset, int length){
        this.array = array;
        this.offset = offset;
        this.length = length;
    }

    public byte[] getArray() {
        return array;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

}
