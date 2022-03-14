package serverGUI;

import im.gui.IServerGUI;
import im.config.Config;
import im.Server;

public class ServerGUI implements IServerGUI {

    final Server server;

    public static void main(String[] args) {
        try{
            Config.updateFromFile();
            new ServerGUI();
        }catch(Exception e){
            e.printStackTrace();
            System.exit(0);
        }
    }

    public ServerGUI(){
        server = new Server(this);
        server.start();
    }

    public void createGUI(){
        ServerFrame serverFrame = new ServerFrame(server);
        serverFrame.setVisible(true);
    }

}
