package PCGUI.components;

import GUI.IFileTransferringPanel;
import PCGUI.helper.PanelUtil;
import mutil.IStringGetter;

import javax.swing.*;
import java.awt.*;

public class FileTransferringPanel extends JPanel implements IFileTransferringPanel {
    private final JTextArea infoArea;
    private final IStringGetter fileNameGetter;

    public FileTransferringPanel(IStringGetter fileNameGetter){
        super();
        this.fileNameGetter = fileNameGetter;
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
        setInfo(String.format("Transferring: %s (%.2f%%)",fileNameGetter.getString(),progress*100));
    }

    public void setInfo(String info){
        infoArea.setText(info);
    }

}
