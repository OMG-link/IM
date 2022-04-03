package com.omg_link.im.core.protocol.data;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.UUID;

public class ByteData implements Cloneable{
    private int begin,length;
    private byte[] data;

    //static

    public static void memcpy(byte[] dest, int destOffset, byte[] src, int srcOffset, int length) {
        if (length >= 0) {
            System.arraycopy(src, srcOffset, dest, destOffset, length);
        }
    }

    private static int byteToInt(byte b){
        return ((int)b)&0xff;
    }

    private static int nextPowOf2(int x){
        if(x<0||x>(1<<30)) {
            throw new InvalidParameterException(String.format("x=%d is not between 0 and 2^30",x));
        }
        if(x==0){
            return 0;
        }else{
            int y = 1;
            while(y<x){
                y <<= 1;
            }
            return y;
        }
    }

    //functions

    @Override
    public ByteData clone(){
        return new ByteData(this);
    }

    public void setData(ByteData data) {
        this.setLength(data.length);
        this.copyBytesFrom(data.data, data.begin, data.length);
    }

    public ByteArrayInfo getData(){
        return new ByteArrayInfo(data,begin,length);
    }

    public byte[] getByteArray(){
        byte[] result = new byte[length];
        copyBytesTo(result,length);
        return result;
    }

    public int getLength() {
        return length;
    }

    /**
     * Set ByteData to target length.
     * @param newLength target length
     */
    public void setLength(int newLength) {
        if(newLength >= data.length/4 && begin+newLength <= data.length){
            this.length = newLength;
        }else{
            resize(newLength);
        }
    }

    private void resize(int newLength) {
        //backup
        int oldBegin = begin;
        int oldLength = length;
        byte[] oldData = data;
        //copy
        byte[] newData = new byte[nextPowOf2(newLength)];
        memcpy(newData,0,oldData,oldBegin,oldLength);
        //format
        begin = 0;
        length = newLength;
        data = newData;
    }

    public ByteData append(ByteData rData) {
        int oldLength = getLength();
        int newLength = oldLength + rData.length;
        setLength(newLength);
        copyBytesFrom(oldLength, rData.data, rData.begin, rData.length);
        return this;
    }

    public ByteData remove(int removedLength) throws InvalidPackageException {
        checkLength(removedLength);
        begin += removedLength;
        length -= removedLength;
        setLength(length); //free memory
        return this;
    }

    /**
     * Split the ByteData into two parts.
     * @param length The length of the first part.
     * @return The ByteData of the first part.
     * @throws InvalidParameterException When the length of old ByteData is less than {@code length} or {@code length} is negative.
     */
    public ByteData cut(int length) throws InvalidParameterException {
        if(length<0||this.length<length) throw new InvalidParameterException();
        ByteData data = new ByteData(this.data,this.begin,length);
        try{
            remove(length);
        }catch (InvalidPackageException e){
            throw new RuntimeException(e); //This never happens.
        }
        return data;
    }

    private void checkLength(int expectLength) throws InvalidPackageException {
        if (this.data.length < expectLength)
            throw new InvalidPackageException();
    }

    private void copyBytesTo(byte[] dest,int destOffset,int length){
        memcpy(dest,destOffset,data,begin,length);
    }

    private void copyBytesTo(byte[] dest,int length){
        copyBytesTo(dest,0,length);
    }

    private void copyBytesFrom(int hereOffset,byte[] src,int srcOffset,int length){
        hereOffset += begin;
        memcpy(data,hereOffset,src,srcOffset,length);
    }

    private void copyBytesFrom(byte[] src,int srcOffset,int length){
        copyBytesFrom(0,src,srcOffset,length);
    }

    private void copyBytesFrom(byte[] src,int length){
        copyBytesFrom(src,0,length);
    }

    private byte getByte(int index){
        return data[begin+index];
    }

    private void setByte(int index,byte b){
        data[begin+index] = b;
    }

    //constructors

    public ByteData() {
        this(0);
    }

    public ByteData(int length) {
        this.begin = 0;
        this.length = length;
        this.data = new byte[nextPowOf2(length)];
    }

    public ByteData(byte[] data){
        this(data,0,data.length);
    }

    public ByteData(byte[] data,int offset,int length){
        this(data.length);
        copyBytesFrom(data,offset,length);
    }

    public ByteData(ByteData byteData){
        this(0);
        this.setData(byteData);
    }

    public ByteData(InputStream inputStream, int length) throws IOException {
        this(0);
        if(inputStream==null){
            throw new IOException("InputStream is null!");
        }
        this.setLength(length);
        int pos = 0;
        while(pos<length){
            int temp = inputStream.read(this.data,pos,length-pos);
            if(temp==-1){
                throw new EOFException();
            }else{
                pos += temp;
            }
        }
    }

    //decoders

    public boolean peekBoolean() throws InvalidPackageException {
        this.checkLength(1);
        return (this.getByte(0)!=0);
    }

    public boolean decodeBoolean() throws InvalidPackageException {
        boolean result = peekBoolean();
        this.remove(1);
        return result;
    }

    public int peekInt() throws InvalidPackageException {
        this.checkLength(4);
        int ans = 0;
        for (int i = 0; i < 4; i++) {
            ans = (ans << 8) | byteToInt(this.getByte(i));
        }
        return ans;
    }

    public int decodeInt() throws InvalidPackageException {
        int result = peekInt();
        this.remove(4);
        return result;
    }

    public long peekLong() throws InvalidPackageException {
        this.checkLength(8);
        long ans = 0;
        for (int i = 0; i < 8; i++) {
            ans = (ans << 8) | byteToInt(this.getByte(i));
        }
        return ans;
    }

    public long decodeLong() throws InvalidPackageException {
        long ans = peekLong();
        this.remove(8);
        return ans;
    }

    public UUID decodeUuid() throws InvalidPackageException {
        this.checkLength(16);
        long highLong = this.decodeLong();
        long lowLong = this.decodeLong();
        return new UUID(highLong,lowLong);
    }

    public String decodeString() throws InvalidPackageException {
        byte[] buffer = this.decodeByteArray();
        return new String(buffer,StandardCharsets.UTF_8);
    }

    public byte[] decodeByteArray() throws InvalidPackageException {
        int length = this.decodeInt();
        this.checkLength(length);
        byte[] buffer = new byte[length];
        this.copyBytesTo(buffer,length);
        this.remove(length);
        return buffer;
    }

    public <E extends Enum<E>> E peekEnum(E[] values) throws InvalidPackageException{
        var index = this.peekInt();
        if(index<0||index>=values.length){
            throw new InvalidPackageException();
        }
        return values[index];
    }

    public <E extends Enum<E>> E decodeEnum(E[] values) throws InvalidPackageException {
        var index = this.decodeInt();
        if(index<0||index>=values.length){
            throw new InvalidPackageException();
        }
        return values[index];
    }

    //append

    public ByteData append(boolean b){
        return append(ByteData.encode(b));
    }

    public ByteData append(int x){
        return append(ByteData.encode(x));
    }

    public ByteData append(long x){
        return append(ByteData.encode(x));
    }

    public ByteData append(UUID uuid){
        return append(ByteData.encode(uuid));
    }

    public ByteData append(String s){
        return append(ByteData.encode(s));
    }

    public ByteData append(byte[] b){
        return append(ByteData.encode(b));
    }

    public <E extends Enum<E>> ByteData append(E e){
        return append(ByteData.encode(e));
    }

    public ByteData append(IEncodeable e){
        return append(ByteData.encode(e));
    }

    public ByteData appendByteArray(byte[] b){
        return append(new ByteData(b));
    }

    //encoders

    public static ByteData encode(boolean b){
        ByteData data = new ByteData(1);
        data.setByte(0,(byte)(b?1:0));
        return data;
    }

    public static ByteData encode(int x) {
        ByteData data = new ByteData(4);
        for (int i = 3; i >= 0; i--) {
            data.setByte(i,(byte) (x & 0xff));
            x >>>= 8;
        }
        return data;
    }

    public static ByteData encode(long x) {
        ByteData data = new ByteData(8);
        for (int i = 7; i >= 0; i--) {
            data.setByte(i,(byte) (x & 0xff));
            x >>>= 8;
        }
        return data;
    }

    public static ByteData encode(UUID x){
        return new ByteData()
                .append(ByteData.encode(x.getMostSignificantBits()))
                .append(ByteData.encode(x.getLeastSignificantBits()));
    }

    public static ByteData encode(String s) {
        byte[] byteString = s.getBytes(StandardCharsets.UTF_8);
        return encode(byteString);
    }

    public static ByteData encode(byte[] bytes){
        return ByteData.encode(bytes.length)
                .appendByteArray(bytes);
    }

    public static <E extends Enum<E>> ByteData encode(E e){
        return encode(e.ordinal());
    }

    public static ByteData encode(IEncodeable e){
        return e.encode();
    }

    //debug

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(String.format("Actual length=%d; Begin=%d; length=%d;\n",data.length,begin,length));
        for (int i = 0; i < this.getLength(); i++) {
            result.append(String.format("%02x ", this.getByte(i)));
        }
        if(length==0){
            result.append("(no data)");
        }
        return result.toString();
    }

}
