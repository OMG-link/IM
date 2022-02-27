package PCGUI.components;

import GUI.IFileTransferringPanel;
import PCGUI.helper.PanelUtil;

import javax.swing.*;
import java.awt.*;

public class FileTransferringPanel extends JPanel implements IFileTransferringPanel {
    private final JTextArea infoArea;
    private final String fileName;

    public FileTransferringPanel(String fileName){
        super();
        this.fileName = fileName;
        this.infoArea = PanelUtil.makeTextArea(
                Color.BLACK,
                12,
                ""
        );

        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.add(infoArea);

        this.setInfo("Transfer task started.");

    }

    public void setProgress(double progress){
        setInfo(String.format("Transferring: %s (%.2f%%)",fileName,progress*100));
    }

    public void setInfo(String info){
        infoArea.setText(info);
    }

}
