package com.omg_link.im.server_gui;

import com.omg_link.im.core.ServerRoom;
import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.gui.IServerGui;
import com.omg_link.sqlite_bridge.xerial.SqlComponentFactory;

public class ServerGui implements IServerGui {

    final ServerRoom serverRoom;

    public static void main(String[] args) {
        try{
            // Process cmd args
            boolean headless = false;
            for (String arg : args) {
                if ("--headless".equals(arg)) {
                    headless = true;
                    break;
                }
            }
            // Start server
            Class.forName("org.sqlite.JDBC");
            Config.updateFromFile();
            new ServerGui(!headless);
        }catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }
    }

    public ServerGui(boolean enableGui){
        serverRoom = new ServerRoom(this);
        if(enableGui){
            createGUI();
        }
    }

    public void createGUI(){
        new ServerFrame(serverRoom);
    }

    @Override
    public SqlComponentFactory getSqlComponentFactory() {
        return new SqlComponentFactory();
    }

}
