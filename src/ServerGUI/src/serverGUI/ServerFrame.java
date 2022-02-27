package serverGUI;

import IM.Server;

import javax.swing.*;
import java.awt.*;

public class ServerFrame extends JFrame {
    private final Server handler;

    public ServerFrame(Server handler){
        this.handler = handler;

        this.setTitle("IM Server");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        makeMainText();

        this.setMinimumSize(new Dimension(300,100));
        this.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(screenSize.width/2-this.getWidth()/2,screenSize.height/2-this.getHeight()/2);

    }

    public void makeMainText(){
        JLabel label = new JLabel();
        label.setText("IM Server is running.");

        this.add(label);

    }

}
