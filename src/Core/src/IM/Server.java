package IM;

import GUI.IServerGUI;
import IM.FactoryManager.ServerFactoryManager;
import mutils.file.ServerFileManager;
import protocol.ServerNetworkHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    ServerFactoryManager factoryManager = new ServerFactoryManager();
    ServerFileManager fileManager;
    ServerNetworkHandler networkHandler;
    final IServerGUI GUI;

    public Server(IServerGUI GUI){
        this.GUI = GUI;
        try{
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

    public ServerFactoryManager getFactoryManager() {
        return factoryManager;
    }

    public ServerFileManager getFileManager() {
        return fileManager;
    }

    public ServerNetworkHandler getNetworkHandler() {
        return networkHandler;
    }

}
