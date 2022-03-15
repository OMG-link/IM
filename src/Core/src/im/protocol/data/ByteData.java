package im.protocol.data;

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

    public boolean decodeBoolean() throws InvalidPackageException {
        return ByteData.decodeBoolean(this);
    }

    public int decodeInt() throws InvalidPackageException {
        return ByteData.decodeInt(this);
    }

    public long decodeLong() throws InvalidPackageException {
        return ByteData.decodeLong(this);
    }

    public UUID decodeUuid() throws InvalidPackageException {
        return ByteData.decodeUuid(this);
    }

    public String decodeString() throws InvalidPackageException {
        return ByteData.decodeString(this);
    }

    //static encode

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
        ByteData data = new ByteData(byteString.length+4);
        data.copyBytesFrom(0,(ByteData.encode(byteString.length)).data,0,4);
        data.copyBytesFrom(4,byteString,0,byteString.length);
        return data;
    }

    public static ByteData encode(byte[] bytes){
        ByteData data = new ByteData(bytes.length+4);
        data.copyBytesFrom(0,(ByteData.encode(bytes.length)).data,0,4);
        data.copyBytesFrom(4,bytes,0,bytes.length);
        return data;
    }

    //static decode and peek

    public static boolean decodeBoolean(ByteData data) throws InvalidPackageException {
        boolean result = peekBoolean(data);
        data.remove(1);
        return result;
    }

    public static boolean peekBoolean(ByteData data) throws InvalidPackageException {
        data.checkLength(1);
        return (data.getByte(0)!=0);
    }

    public static int decodeInt(ByteData data) throws InvalidPackageException {
        int result = peekInt(data);
        data.remove(4);
        return result;
    }

    public static int peekInt(ByteData data) throws InvalidPackageException {
        data.checkLength(4);
        int ans = 0;
        for (int i = 0; i < 4; i++) {
            ans = (ans << 8) | byteToInt(data.getByte(i));
        }
        return ans;
    }

    public static long decodeLong(ByteData data) throws InvalidPackageException {
        data.checkLength(8);
        long ans = 0;
        for (int i = 0; i < 8; i++) {
            ans = (ans << 8) | byteToInt(data.getByte(i));
        }
        data.remove(8);
        return ans;
    }

    public static UUID decodeUuid(ByteData data) throws InvalidPackageException {
        data.checkLength(16);
        long highLong = ByteData.decodeLong(data);
        long lowLong = ByteData.decodeLong(data);
        return new UUID(highLong,lowLong);
    }

    public static String decodeString(ByteData data) throws InvalidPackageException {
        byte[] buffer = decodeByteArray(data);
        return new String(buffer,StandardCharsets.UTF_8);
    }

    public static byte[] decodeByteArray(ByteData data) throws InvalidPackageException {
        int length = decodeInt(data);
        data.checkLength(length);
        byte[] buffer = new byte[length];
        data.copyBytesTo(buffer,length);
        data.remove(length);
        return buffer;
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
