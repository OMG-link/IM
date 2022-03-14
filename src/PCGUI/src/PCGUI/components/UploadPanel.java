package PCGUI.components;

import im.gui.IFileTransferringPanel;
import PCGUI.helper.PanelUtil;
import mutils.FileUtils;
import mutils.IStringGetter;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class UploadPanel extends JPanel implements IFileTransferringPanel {
    private final JTextArea infoArea;
    private final IStringGetter fileNameGetter;
    private final long fileSize;

    public UploadPanel(IStringGetter fileNameGetter,long fileSize){
        super();
        this.fileNameGetter = fileNameGetter;
        this.fileSize = fileSize;
        this.infoArea = PanelUtil.makeTextArea(
                Color.BLACK,
                12,
                ""
        );

        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.add(infoArea);
    }

    @Override
    public void setProgress(long progress){
        setInfo(String.format("Uploading %s (%s/%s)",fileNameGetter.getString(), FileUtils.sizeToString(progress), FileUtils.sizeToString(fileSize)));
    }

    @Override
    public void onTransferStart() {
        setInfo("Starting to upload...");
    }

    @Override
    public void onTransferSucceed(File file) {
        this.setVisible(false);
    }

    @Override
    public void onTransferFailed(String reason) {
        setInfo("File upload failed: "+reason);
    }

    public void setInfo(String info){
        infoArea.setText(info);
    }

}
