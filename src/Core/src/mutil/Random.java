package mutil;

public class Random {
    public static int randomInt(int l,int r){
        return (int)(Math.random()*(r-l+1))+l;
    }

}
