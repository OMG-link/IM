package PCGUI.components;

import javax.swing.*;
import java.awt.*;

public class ImagePanel extends JPanel {
    private final ImageIcon imageIcon;
    private final Dimension panelSize;

    public ImagePanel(ImageIcon imageIcon){
        this.imageIcon = imageIcon;
        int imageHeight = imageIcon.getIconHeight();
        int imageWidth = imageIcon.getIconWidth();
        int panelHeight = Math.min(imageIcon.getIconHeight(),200);
        int panelWidth = panelHeight*imageWidth/imageHeight;
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
