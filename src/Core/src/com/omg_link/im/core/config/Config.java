package com.omg_link.im.core.config;

import com.omg_link.utils.Random;

import java.awt.*;
import java.io.*;
import java.security.InvalidParameterException;
import java.util.Scanner;

public class Config {
    public static final String version = "1.5.1 beta";
    public static final String compatibleVersion = "1.5.0";

    public static final int packageMaxLength = 10 * 1024 * 1024; //10MB
    public static final int chatTextMaxLength = 10 * 1024; //10KB
    public static final int nickMaxLength = 20;
    public static final int bufferedRecordNum = 100;
    public static final long fileMaxSize = 1024 * 1024 * 1024; //1GB
    public static final int recordsPerPage = 15;

    private static int serverPort = 8814;

    private static String url = "www.omg-link.com:8814";
    private static String serverIP = "127.0.0.1";
    private static String username = String.format("User %d", Random.randomInt(1000, 9999));

    private static String token = "official";
    private static String roomName = "";
    private static String serverDatabasePath = "serverChatLog.db";

    private static String runtimeDir = toDirName(".");
    private static String cacheDir = toDirName("IMCache");

    private static String getConfigFileName() {
        return "{runtimeDir}/config.txt"
                .replace("{runtimeDir}", Config.getRuntimeDir());
    }

    public static void updateFromFile() {
        File file = new File(getConfigFileName());

        try (FileInputStream inputStream = new FileInputStream(file)) {
            Scanner scanner = new Scanner(inputStream);
            while (scanner.hasNext()) {
                String s = scanner.nextLine();
                int pos = s.indexOf('=');
                if (pos == -1) continue;
                String name = s.substring(0, pos);
                String value = s.substring(pos + 1);
                try {
                    switch (name) {
                        case "url": {
                            setUrl(value);
                            break;
                        }
                        case "username": {
                            setUsername(value);
                            break;
                        }
                        case "serverIp": {
                            setUrl(value + ":" + serverPort);
                            break;
                        }
                        case "port": {
                            setUrl(serverIP + ":" + value);
                            break;
                        }
                        case "token": {
                            setToken(value);
                            break;
                        }
                        case "roomName": {
                            setRoomName(value);
                            break;
                        }
                        case "serverDatabasePath": {
                            setServerDatabasePath(value);
                            break;
                        }
                    }
                } catch (ConfigSetFailedException ignored) {
                }
            }
        } catch (IOException ignored) {
        }

    }

    public static void saveToFile() {
        File file = new File(getConfigFileName());

        try {
            if (file.exists()) {
                if (!file.delete()) return;
            }
            if (!file.createNewFile()) return;
            FileOutputStream outputStream = new FileOutputStream(file);
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.printf("username=%s\n", getUsername());
            printWriter.printf("serverIp=%s\n", getServerIP());
            printWriter.printf("port=%s\n", getServerPort());
            printWriter.printf("token=%s\n", getToken());
            printWriter.printf("roomName=%s\n", getRoomName());
            printWriter.printf("serverDatabasePath=%s\n", getServerDatabasePath());
            printWriter.close();
            outputStream.close();
        } catch (IOException ignored) {
        }

    }

    public static Font getPreferredFont() {
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
        try {
            int dPos = url.indexOf(':');
            if (dPos == -1) throw new InvalidParameterException();
            Config.setServerIP(url.substring(0, dPos));
            Config.setServerPort(Integer.parseInt(url.substring(dPos + 1)));
            if (Config.getServerPort() < 0 || Config.getServerPort() >= 65536) {
                throw new NumberFormatException();
            }
            Config.url = url;
        } catch (InvalidParameterException e) {
            throw new ConfigSetFailedException(ConfigSetFailedException.Reason.InvalidUrl);
        } catch (NumberFormatException e) {
            throw new ConfigSetFailedException(ConfigSetFailedException.Reason.InvalidPort);
        }
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) throws ConfigSetFailedException {
        if (username.length() > Config.nickMaxLength) {
            throw new ConfigSetFailedException(ConfigSetFailedException.Reason.UsernameTooLong);
        }
        Config.username = username;
    }

    public static String getRoomName() {
        return roomName;
    }

    public static void setRoomName(String roomName) {
        Config.roomName = roomName;
    }

    private static String toDirName(String dirName) {
        dirName = dirName.replace('\\', '/');
        while (dirName.length() > 0 && dirName.charAt(dirName.length() - 1) == '/') {
            dirName = dirName.substring(0, dirName.length() - 1);
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

    public static void setServerDatabasePath(String serverDatabasePath) {
        Config.serverDatabasePath = serverDatabasePath;
    }

    public static String getServerDatabasePath() {
        return serverDatabasePath;
    }

}
