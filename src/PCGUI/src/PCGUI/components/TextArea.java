package PCGUI.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TextArea extends JTextArea {
    private final JPopupMenu popupMenu;

    public TextArea(Color color, int fontSize, String text){
        super();
        Font font = new Font("黑体", Font.PLAIN, fontSize);

        this.setFont(font);
        this.setForeground(color);
        this.setText(text);

        this.setEditable(false);
        this.setLineWrap(true);
        this.setWrapStyleWord(true);
        this.setTabSize(4);

        popupMenu = new JPopupMenu();

        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.addActionListener(e->this.copy());
        popupMenu.add(copyItem);

        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton()==MouseEvent.BUTTON3) {
                    popupMenu.show(e.getComponent(),e.getX(), e.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

    }

}
