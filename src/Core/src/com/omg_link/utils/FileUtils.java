package com.omg_link.utils;

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

}
