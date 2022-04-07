package com.omg_link.im.server_gui;

import com.omg_link.im.core.ServerRoom;
import com.omg_link.im.core.gui.IServerGUI;
import com.omg_link.im.core.config.Config;

public class ServerGUI implements IServerGUI {

    final ServerRoom serverRoom;

    public static void main(String[] args) {
        try{
            Class.forName("org.sqlite.JDBC");
            Config.updateFromFile();
            new ServerGUI();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }
    }

    public ServerGUI(){
        serverRoom = new ServerRoom(this);
    }

    public void createGUI(){
        new ServerFrame(serverRoom);
    }

}
