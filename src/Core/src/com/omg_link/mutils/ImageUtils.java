package com.omg_link.mutils;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

public class ImageUtils {
    public static boolean isImageFile(File file){
        String fileName = file.getName();
        int idx = fileName.lastIndexOf('.');
        if(idx==-1) return false;
        String fileExtension = fileName.substring(idx+1).toLowerCase(Locale.ROOT);
        String[] imgExtensions = {"png","jpg"};
        for(String extension: imgExtensions){
            if(Objects.equals(extension,fileExtension)){
                return true;
            }
        }
        return false;
    }
}
