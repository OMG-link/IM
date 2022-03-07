package IM;

import GUI.IServerGUI;
import mutils.file.ServerFileManager;
import mutils.uuidLocator.UUIDManager;
import protocol.ServerNetworkHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    UUIDManager uuidManager;
    ServerFileManager fileManager;
    ServerNetworkHandler networkHandler;
    IServerGUI GUI;

    public Server(IServerGUI GUI){
        this.GUI = GUI;
        try{
            this.uuidManager = new UUIDManager();
            this.fileManager = new ServerFileManager();
        }catch (Exception e){
            Logger.getGlobal().log(Level.SEVERE,"Unable to create server instance.");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void start(){
        networkHandler = new ServerNetworkHandler(this);
        networkHandler.start();
        if(GUI!=null){
            GUI.createGUI();
        }
    }

    public ServerFileManager getFileManager() {
        return fileManager;
    }

    public UUIDManager getUuidManager() {
        return uuidManager;
    }

    public ServerNetworkHandler getNetworkHandler() {
        return networkHandler;
    }

}
