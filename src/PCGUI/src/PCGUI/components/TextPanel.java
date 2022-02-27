package PCGUI.components;

import PCGUI.helper.PanelUtil;

import javax.swing.*;
import java.awt.*;

public class TextPanel extends JPanel {
    public TextPanel(long stamp,String sender,String text){
        super();

        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.add(PanelUtil.makeMessageInfo(sender,stamp));
        this.add(PanelUtil.makeTextArea(
           Color.BLACK,
           22,
           text
        ));

    }

}
