package im.config;

import mutils.Random;

import java.awt.*;
import java.io.*;
import java.security.InvalidParameterException;
import java.util.Scanner;

public class Config {
    public static final String version = "1.4.0 rc2";
    public static final String compatibleVersion = "1.4.0";

    private static String url = "www.omg-link.com:8814";
    private static String serverIP = "127.0.0.1";
    private static int serverPort = 8814;
    private static String username = String.format("User %d", Random.randomInt(1000,9999));
    private static String token = "official";

    private static String roomName = "";

    public static final int packageMaxLength = 10*1024*1024; //10MB
    public static final long fileMaxSize = 1024*1024*1024; //1GB
    public static final int recordsMaxLength = 100; //100 messages
    public static final int nickMaxLength = 20;

    private static String runtimeDir = toDirName(".");
    private static String cacheDir = toDirName("IMCache");

    private static String getConfigFileName(){
        return Config.getRuntimeDir()+"config.txt";
    }

    public static void updateFromFile(){
        File file = new File(getConfigFileName());

        try(FileInputStream inputStream = new FileInputStream(file)){
            Scanner scanner = new Scanner(inputStream);
            while(scanner.hasNext()){
                String s = scanner.nextLine();
                int pos = s.indexOf('=');
                if(pos==-1) continue;
                String name = s.substring(0,pos);
                String value = s.substring(pos+1);
                try{
                    switch (name){
                        //client
                        case "username":{
                            setUsername(value);
                            break;
                        }
                        case "url":{
                            setUrl(value);
                            break;
                        }
                        case "serverIp":{
                            setUrl(value+":"+serverPort);
                            break;
                        }
                        case "port":{
                            setUrl(serverIP+":"+value);
                            break;
                        }
                        //server
                        case "token":{
                            setToken(value);
                            break;
                        }
                        case "roomName":{
                            setRoomName(value);
                            break;
                        }
                    }
                }catch (ConfigSetFailedException ignored){
                }
            }
        }catch (IOException ignored){}

    }

    public static void saveToFile(){
        File file = new File(getConfigFileName());

        try{
            if(file.exists()){
                if(!file.delete()) return;
            }
            if(!file.createNewFile()) return;
            FileOutputStream outputStream = new FileOutputStream(file);
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.printf("username=%s\n", getUsername());
            printWriter.printf("serverIp=%s\n", getServerIP());
            printWriter.printf("port=%s\n", getServerPort());
            printWriter.printf("token=%s\n",getToken());
            printWriter.close();
            outputStream.close();
        }catch (IOException ignored){}

    }

    public static Font getPreferredFont(){
        return new Font("SimHei", Font.PLAIN, 22);
    }

    public static String getServerIP() {
        return serverIP;
    }

    public static void setServerIP(String serverIP) {
        Config.serverIP = serverIP;
    }

    public static int getServerPort() {
        return serverPort;
    }

    public static void setServerPort(int serverPort) {
        Config.serverPort = serverPort;
    }

    public static String getUrl() {
        return url;
    }

    public static void setUrl(String url) throws ConfigSetFailedException {
        try{
            int dPos = url.indexOf(':');
            if(dPos ==-1) throw new InvalidParameterException();
            Config.setServerIP(url.substring(0, dPos));
            Config.setServerPort(Integer.parseInt(url.substring(dPos +1)));
            if(Config.getServerPort() <0|| Config.getServerPort() >=65536){
                throw new NumberFormatException();
            }
            Config.url = url;
        }catch(InvalidParameterException e){
            throw new ConfigSetFailedException("Invalid URL!");
        }catch(NumberFormatException e){
            throw new ConfigSetFailedException("Invalid Port!");
        }
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) throws InvalidUserNameException {
        if(username.length()>Config.nickMaxLength){
            throw new InvalidUserNameException();
        }
        Config.username = username;
    }

    public static String getRoomName() {
        return roomName;
    }

    public static void setRoomName(String roomName) {
        Config.roomName = roomName;
    }

    private static String toDirName(String dirName){
        if(dirName.length()!=0){
            if(System.getProperty("os.name").toUpperCase().contains("WINDOWS")){
                dirName = dirName.replace('/','\\');
                if(dirName.charAt(dirName.length()-1)!='\\'){
                    dirName += '\\';
                }
            }else{
                dirName = dirName.replace('\\','/');
                if(dirName.charAt(dirName.length()-1)!='/'){
                    dirName += '/';
                }
            }
        }
        return dirName;
    }

    public static String getRuntimeDir() {
        return runtimeDir;
    }

    public static void setRuntimeDir(String runtimeDir) {
        runtimeDir = toDirName(runtimeDir);
        Config.runtimeDir = runtimeDir;
    }

    public static String getCacheDir() {
        return cacheDir;
    }

    public static void setCacheDir(String cacheDir) {
        cacheDir = toDirName(cacheDir);
        Config.cacheDir = cacheDir;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        Config.token = token;
    }

}
