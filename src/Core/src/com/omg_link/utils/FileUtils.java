package com.omg_link.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    public static void makeFolder(String folder) throws IOException {
        makeFolder(new File(folder));
    }

    /**
     * @throws IOException When the target operation cannot be performed.
     */
    public static void makeFolder(File folder) throws IOException {
        if(!folder.isAbsolute()){
            makeFolder(folder.getAbsoluteFile());
            return;
        }
        if(!folder.exists()){
            makeFolder(folder.getParentFile());
            if(!folder.mkdir()){
                throw new IOException("Unable to create folder.");
            }
        }else{
            if(!folder.isDirectory()){
                throw new IOException("Target folder is a file.");
            }
        }
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
        try(var fileInputStream = new FileInputStream(file)){
            byte[] buffer = new byte[10*1024*1024];
            while(true){
                int len = fileInputStream.read(buffer);
                if(len==-1) break;
                messageDigest.update(buffer,0,len);
            }
            return new Sha512Digest(messageDigest.digest());
        }
    }

}












