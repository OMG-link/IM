package protocol.helper.data;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Data {
    private byte[] data;

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

    public Data() {
        this.data = new byte[0];
    }

    public Data(byte[] bytes){
        this.data = new byte[bytes.length+4];
        memcpy(this.data,0,(new Data(bytes.length)).data);
        memcpy(this.data,4,bytes);
    }

    public Data(InputStream inputStream,int length) throws IOException {
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

    public Data(boolean b){
        this.data = new byte[1];
        this.data[0] = (byte)(b?1:0);
    }

    public Data(int x) {
        this.data = new byte[4];
        for (int i = 3; i >= 0; i--) {
            this.data[i] = (byte) (x & 0xff);
            x >>>= 8;
        }
    }

    public Data(long x) {
        this.data = new byte[8];
        for (int i = 7; i >= 0; i--) {
            this.data[i] = (byte) (x & 0xff);
            x >>>= 8;
        }
    }

    public Data(UUID x){
        this(x.getMostSignificantBits());
        Data dLow = Data.encodeLong(x.getLeastSignificantBits());
        this.append(dLow);
    }

    public Data(String s) {
        byte[] string = s.getBytes(StandardCharsets.UTF_8);
        this.data = (new Data(string)).data;
    }

    public Data append(Data rData) {
        byte[] lhs = this.data;
        byte[] rhs = rData.data;
        this.data = new byte[lhs.length + rhs.length];
        memcpy(this.data, 0, lhs);
        memcpy(this.data, lhs.length, rhs);
        return this;
    }

    public Data remove(int length) throws InvalidPackageException {
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

    public int length() {
        return this.data.length;
    }

    public void resize(int n) {
        byte[] nw = new byte[n];
        memcpy(nw, 0, this.data, 0, Math.min(this.data.length, n));
        this.data = nw;
    }

    public static Data encodeBoolean(boolean b){
        return new Data(b);
    }

    public static Data encodeInt(int x) {
        return new Data(x);
    }

    public static Data encodeLong(long x) {
        return new Data(x);
    }

    public static Data encodeUUID(UUID x){
        return new Data(x);
    }

    public static Data encodeString(String s) {
        return new Data(s);
    }

    public static Data encodeByteArray(byte[] bytes){
        return new Data(bytes);
    }

    public static boolean decodeBoolean(Data data) throws InvalidPackageException {
        boolean result = peekBoolean(data);
        data.remove(1);
        return result;
    }

    public static boolean peekBoolean(Data data) throws InvalidPackageException {
        data.checkLength(1);
        boolean b = (data.data[0]!=0);
        return b;
    }

    public static int decodeInt(Data data) throws InvalidPackageException {
        int result = peekInt(data);
        data.remove(4);
        return result;
    }

    public static int peekInt(Data data) throws InvalidPackageException {
        data.checkLength(4);
        int ans = 0;
        for (int i = 0; i < 4; i++) {
            ans = (ans << 8) | byteToInt(data.data[i]);
        }
        return ans;
    }

    public static long decodeLong(Data data) throws InvalidPackageException {
        data.checkLength(8);
        long ans = 0;
        for (int i = 0; i < 8; i++) {
            ans = (ans << 8) | byteToInt(data.data[i]);
        }
        data.remove(8);
        return ans;
    }

    public static UUID decodeUuid(Data data) throws InvalidPackageException {
        data.checkLength(16);
        long highLong = Data.decodeLong(data);
        long lowLong = Data.decodeLong(data);
        return new UUID(highLong,lowLong);
    }

    public static String decodeString(Data data) throws InvalidPackageException {
        byte[] buffer = decodeByteArray(data);
        return new String(buffer,StandardCharsets.UTF_8);
    }

    public static byte[] decodeByteArray(Data data) throws InvalidPackageException {
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
