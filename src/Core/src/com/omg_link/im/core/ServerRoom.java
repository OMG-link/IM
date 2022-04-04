package com.omg_link.im.core;

import com.omg_link.im.core.factory_manager.ServerFactoryManager;
import com.omg_link.im.core.gui.IServerGUI;
import com.omg_link.im.core.user_manager.ServerUserManager;
import com.omg_link.im.core.file_manager.ServerFileManager;
import com.omg_link.im.core.protocol.ServerNetworkHandler;
import com.omg_link.im.core.message_manager.ServerMessageManager;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerRoom {
    final Logger logger = Logger.getLogger("IMServer");
    final ServerFactoryManager factoryManager = new ServerFactoryManager();
    final ServerMessageManager messageManager = new ServerMessageManager(this);
    final ServerUserManager userManager = new ServerUserManager(this);
    final ServerNetworkHandler networkHandler;
    final IServerGUI GUI;
    final ServerFileManager fileManager;

    public final UUID serverId;

    public ServerRoom(IServerGUI GUI){
        try{
            this.GUI = GUI;
            this.serverId = messageManager.getDialogId();
            this.fileManager = new ServerFileManager(serverId);
            this.networkHandler = new ServerNetworkHandler(this);
            if(GUI!=null){
                GUI.createGUI();
            }
        }catch (Exception e){
            logger.log(
                    Level.SEVERE,
                    "Unable to create server instance."
            );
            throw new RuntimeException(e);
        }
    }

    public void close(){
        messageManager.close();
        System.exit(0);
    }

    public Logger getLogger() {
        return logger;
    }

    public ServerUserManager getUserManager() {
        return userManager;
    }

    public ServerMessageManager getMessageManager() {
        return messageManager;
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
