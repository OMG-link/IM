package com.omg_link.im.pc_gui.components;

import javax.swing.*;
import java.awt.*;

public class ImagePanel extends JPanel {
    private final ImageIcon imageIcon;
    private final Dimension panelSize;

    public ImagePanel(ImageIcon imageIcon){
        this.imageIcon = imageIcon;
        int imageHeight = imageIcon.getIconHeight();
        int imageWidth = imageIcon.getIconWidth();
        float scale = Math.min(200f/imageHeight,600f/imageWidth);
        if(scale>1) scale = 1;
        int panelHeight = (int)(imageHeight*scale);
        int panelWidth = (int)(imageWidth*scale);
        this.panelSize = new Dimension(panelWidth,panelHeight);
    }

    @Override
    public Dimension getPreferredSize(){
        return panelSize;
    }

    @Override
    protected void paintComponent(Graphics graphics){
        super.paintComponent(graphics);
        graphics.drawImage(imageIcon.getImage(),0,0,panelSize.width,panelSize.height,imageIcon.getImageObserver());
    }

}
