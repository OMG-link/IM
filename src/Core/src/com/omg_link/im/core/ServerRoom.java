package com.omg_link.im.core;

import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.factory_manager.ServerFactoryManager;
import com.omg_link.im.core.file_manager.ServerFileManager;
import com.omg_link.im.core.gui.IServerGUI;
import com.omg_link.im.core.message_manager.ServerMessageManager;
import com.omg_link.im.core.protocol.ServerNetworkHandler;
import com.omg_link.im.core.sql_manager.server.ServerSqlManager;
import com.omg_link.im.core.user_manager.ServerUserManager;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerRoom {
    final Logger logger = Logger.getLogger("IMServer");
    final ServerFactoryManager factoryManager = new ServerFactoryManager();
    final ServerMessageManager messageManager;
    final ServerUserManager userManager = new ServerUserManager(this);;
    final ServerNetworkHandler networkHandler;
    final IServerGUI GUI;
    final ServerFileManager fileManager;

    public final UUID serverId;
    ServerSqlManager sqlManager;

    public ServerRoom(IServerGUI GUI){
        this.GUI = GUI;
        try{
            try {
                sqlManager = new ServerSqlManager(Config.getServerDatabasePath());
            } catch (SQLException e) {
                e.printStackTrace();
                getLogger().log(
                        Level.WARNING,
                        "Cannot open database file. Chat history will not be logged."
                );
            }
            boolean isSqlEnabled = sqlManager!=null;

            this.messageManager = new ServerMessageManager(this,isSqlEnabled);
            this.serverId = isSqlEnabled?sqlManager.getDatabaseUuid():UUID.randomUUID();

            this.fileManager = new ServerFileManager(this,serverId,isSqlEnabled);
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
        closeSqlManager();
    }

    private void closeSqlManager(){
        messageManager.disableSql();
        fileManager.disableSql();
        sqlManager.close();
        sqlManager = null;
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

    public ServerSqlManager getSqlManager() {
        return sqlManager;
    }

}
