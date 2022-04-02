package serverGUI;

import com.omg_link.im.gui.IServerGUI;
import com.omg_link.im.config.Config;
import com.omg_link.im.Server;

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
    }

    public void createGUI(){
        ServerFrame serverFrame = new ServerFrame(server);
        serverFrame.setVisible(true);
    }

}
