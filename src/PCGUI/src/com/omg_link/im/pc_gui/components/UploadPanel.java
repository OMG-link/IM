package com.omg_link.im.pc_gui.components;

import com.omg_link.im.core.gui.IFileTransferringPanel;
import com.omg_link.im.pc_gui.helper.PanelUtil;
import com.omg_link.utils.FileUtils;
import com.omg_link.utils.IStringGetter;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

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
    public void onTransferSucceed(UUID senderFileId, UUID receiverFileId) {
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
