package com.omg_link.im.server_gui;

import com.omg_link.im.core.ServerRoom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ServerFrame extends JFrame {
    private final ServerRoom room;

    public ServerFrame(ServerRoom room_){
        this.room = room_;

        this.setTitle("IM Server");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {
                room.close();
            }
            @Override
            public void windowClosed(WindowEvent e) {}
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });

        makeMainText();

        this.setMinimumSize(new Dimension(300,100));
        this.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(screenSize.width/2-this.getWidth()/2,screenSize.height/2-this.getHeight()/2);

        this.setVisible(true);

    }

    public void makeMainText(){
        JLabel label = new JLabel();
        label.setText("IM Server is running.");

        this.add(label);

    }

}
