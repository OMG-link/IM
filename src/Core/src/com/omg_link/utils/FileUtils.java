package com.omg_link.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {

    public static String sizeToString(long size){
        if(size<1000L){
            return String.format("%dB",size);
        }else if(size<(long)1e6){
            return String.format("%.2fKB",(double)size/1e3);
        }else if(size<(long)1e9){
            return String.format("%.2fMB",(double)size/1e6);
        }else if(size<(long)1e12){
            return String.format("%.2fGB",(double)size/1e9);
        }else{
            return String.format("%.2fTB",(double)size/1e12);
        }
    }

    public static boolean isFileNameLegal(String fileName){
        char[] bannedCharSet = {
                '/','\\',':','*','?','"','<','>','|'
        };
        for(Character c:bannedCharSet){
            if(fileName.contains(c.toString())){
                return false;
            }
        }
        return true;
    }

    /**
     * Calculate the SHA-512 for the file.
     * @return SHA-512 Digest
     * @throws FileNotFoundException When the file does not exist.
     * @throws IOException When an I/O error occurs during reading the file.
     */
    public static Sha512Digest sha512(File file) throws IOException {
        MessageDigest messageDigest;
        try{
            messageDigest = MessageDigest.getInstance("SHA-512");
        }catch (NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
        var fileInputStream = new FileInputStream(file);
        var fileChannel = fileInputStream.getChannel();
        var mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY,0,file.length());
        messageDigest.update(mappedByteBuffer);
        return new Sha512Digest(messageDigest.digest());
    }

}












