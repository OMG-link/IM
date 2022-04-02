package com.omg_link.im.pc_gui.helper;

import com.omg_link.im.pc_gui.components.TextArea;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PanelUtil {
    public static JTextArea makeTextArea(Color color, int fontSize, String text){
        return new TextArea(color,fontSize,text);
    }

    public static JTextArea makeMessageInfo(String sender,long stamp){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return makeTextArea(
                new Color(34,139,34),
                14,
                String.format("%s  %s",sender,format.format(new Date(stamp)))
        );
    }

}
