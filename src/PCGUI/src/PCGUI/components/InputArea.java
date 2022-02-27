package PCGUI.components;

import IM.Config;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class InputArea extends JTextArea {
    private final JPopupMenu popupMenu;
    private final IInputCallback callback;

    public InputArea(IInputCallback callback){
        this.callback = callback;
        this.setFont(Config.getPreferredFont());
        this.setTabSize(4);

        popupMenu = new JPopupMenu();

        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.addActionListener(e->this.copy());
        popupMenu.add(copyItem);

        JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.addActionListener(e->this.paste());
        popupMenu.add(pasteItem);

        JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.addActionListener(e->this.cut());
        popupMenu.add(cutItem);

        this.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton()==MouseEvent.BUTTON3) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
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

    @Override
    protected void processKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER: {
                    if (e.isControlDown()) {
                        this.insert("\n",this.getCaretPosition());
                    } else {
                        this.callback.onInputFinish();
                    }
                    return;
                }
            }
        }
        super.processKeyEvent(e);
    }
}
