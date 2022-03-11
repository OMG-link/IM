package protocol.helper.data;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ByteData implements Cloneable{
    private byte[] data;

    @Override
    public ByteData clone(){
        return new ByteData(this);
    }

    private void checkLength(int expectLength) throws InvalidPackageException {
        if (this.data.length < expectLength)
            throw new InvalidPackageException();
    }

    public static void memcpy(byte[] lhs, int offset, byte[] rhs) {
        memcpy(lhs, offset, rhs, 0, rhs.length);
    }

    public static void memcpy(byte[] lhs, int lOffset, byte[] rhs, int rOffset, int length) {
        if (length >= 0)
            System.arraycopy(rhs, rOffset, lhs, lOffset, length);
    }

    private static int byteToInt(byte b){
        return ((int)b)&0xff;
    }

    public ByteData() {
        this.data = new byte[0];
    }

    public ByteData(ByteData byteData){
        this.setData(byteData.getData());
    }

    public ByteData(byte[] bytes){
        this.data = new byte[bytes.length+4];
        memcpy(this.data,0,(new ByteData(bytes.length)).data);
        memcpy(this.data,4,bytes);
    }

    public ByteData(InputStream inputStream, int length) throws IOException {
        if(inputStream==null){
            throw new IOException("InputStream is null!");
        }
        this.data = new byte[length];
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

    public ByteData(boolean b){
        this.data = new byte[1];
        this.data[0] = (byte)(b?1:0);
    }

    public ByteData(int x) {
        this.data = new byte[4];
        for (int i = 3; i >= 0; i--) {
            this.data[i] = (byte) (x & 0xff);
            x >>>= 8;
        }
    }

    public ByteData(long x) {
        this.data = new byte[8];
        for (int i = 7; i >= 0; i--) {
            this.data[i] = (byte) (x & 0xff);
            x >>>= 8;
        }
    }

    public ByteData(UUID x){
        this(x.getMostSignificantBits());
        ByteData dLow = ByteData.encode(x.getLeastSignificantBits());
        this.append(dLow);
    }

    public ByteData(String s) {
        byte[] string = s.getBytes(StandardCharsets.UTF_8);
        this.data = (new ByteData(string)).data;
    }

    public ByteData append(ByteData rData) {
        byte[] lhs = this.data;
        byte[] rhs = rData.data;
        this.data = new byte[lhs.length + rhs.length];
        memcpy(this.data, 0, lhs);
        memcpy(this.data, lhs.length, rhs);
        return this;
    }

    public ByteData remove(int length) throws InvalidPackageException {
        checkLength(length);
        int nLength = this.data.length - length;
        byte[] lhs = this.data;
        this.data = new byte[nLength];
        memcpy(this.data, 0, lhs, length, nLength);
        return this;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = new byte[data.length];
        memcpy(this.data,0,data);
    }

    public int length() {
        return this.data.length;
    }

    public void resize(int n) {
        byte[] nw = new byte[n];
        memcpy(nw, 0, this.data, 0, Math.min(this.data.length, n));
        this.data = nw;
    }

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

    public static ByteData encode(boolean b){
        return new ByteData(b);
    }

    public static ByteData encode(int x) {
        return new ByteData(x);
    }

    public static ByteData encode(long x) {
        return new ByteData(x);
    }

    public static ByteData encode(UUID x){
        return new ByteData(x);
    }

    public static ByteData encode(String s) {
        return new ByteData(s);
    }

    public static ByteData encode(byte[] bytes){
        return new ByteData(bytes);
    }

    public static boolean decodeBoolean(ByteData data) throws InvalidPackageException {
        boolean result = peekBoolean(data);
        data.remove(1);
        return result;
    }

    public static boolean peekBoolean(ByteData data) throws InvalidPackageException {
        data.checkLength(1);
        return (data.data[0]!=0);
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
            ans = (ans << 8) | byteToInt(data.data[i]);
        }
        return ans;
    }

    public static long decodeLong(ByteData data) throws InvalidPackageException {
        data.checkLength(8);
        long ans = 0;
        for (int i = 0; i < 8; i++) {
            ans = (ans << 8) | byteToInt(data.data[i]);
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
        memcpy(buffer,0, data.data,0,length);
        data.remove(length);
        return buffer;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < this.length(); i++) {
            result.append(String.format("%02x ", this.data[i]));
        }
        if(result.toString().equals("")){
            result = new StringBuilder("(no data)");
        }
        return result.toString();
    }

}
