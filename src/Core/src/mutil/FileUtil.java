package mutil;

public class FileUtil {
    public static String sizeToString(long size){
        if(size<1000L){
            return String.format("%dB",size);
        }else if(size<(1L<<10)*1000L){
            return String.format("%.2fKB",(double)size/(1L<<10));
        }else if(size<(1L<<20)*1000L){
            return String.format("%.2fMB",(double)size/(1L<<20));
        }else if(size<(1L<<30)*1000L){
            return String.format("%.2fGB",(double)size/(1L<<30));
        }else{
            return String.format("%.2fTB",(double)size/(1L<<40));
        }
    }
}
