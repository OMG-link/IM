package com.omg_link.utils;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.IEncodeable;
import com.omg_link.im.core.protocol.data.InvalidPackageException;

import java.security.InvalidParameterException;
import java.util.Arrays;

public class Sha512Digest implements IEncodeable {
    private static final int byteArrayLength = 512/8;

    private final byte[] data;

    public Sha512Digest(byte[] data) throws InvalidParameterException {
        if(data.length!= byteArrayLength){
            throw new InvalidParameterException();
        }
        this.data = data.clone();
    }

    public Sha512Digest(ByteData data) throws InvalidPackageException {
        this.data = data.decodeBytes(byteArrayLength);
    }

    @Override
    public ByteData encode() {
        return ByteData.encodeBytes(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sha512Digest that = (Sha512Digest) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

}
