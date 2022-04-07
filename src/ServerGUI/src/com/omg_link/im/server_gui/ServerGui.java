package com.omg_link.im.server_gui;

import com.omg_link.im.core.ServerRoom;
import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.gui.IServerGui;
import com.omg_link.sqlite_bridge.SqlComponentFactory;

public class ServerGui implements IServerGui {

    final ServerRoom serverRoom;

    public static void main(String[] args) {
        try{
            Class.forName("org.sqlite.JDBC");
            Config.updateFromFile();
            new ServerGui();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }
    }

    public ServerGui(){
        serverRoom = new ServerRoom(this);
    }

    public void createGUI(){
        new ServerFrame(serverRoom);
    }

    @Override
    public SqlComponentFactory getSqlComponentFactory() {
        return new SqlComponentFactory();
    }

}
