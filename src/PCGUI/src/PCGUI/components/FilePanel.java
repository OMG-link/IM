package PCGUI.components;

import GUI.IFileTransferringPanel;
import IM.Client;
import PCGUI.helper.PanelUtil;
import mutil.FileUtil;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.io.File;
import java.util.UUID;

public class FilePanel extends JPanel implements IFileTransferringPanel {
    private final Client handler;
    private final long fileSize;

    private final DownloadPanel downloadPanel;

    public FilePanel(Client handler, String sender, long stamp, UUID fileId, String fileName, long fileSize) {
        super();
        this.handler = handler;
        this.fileSize = fileSize;

        this.downloadPanel = makeDownloadPanel(fileName, fileId);
        this.downloadPanel.setBeforeDownload();

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(PanelUtil.makeMessageInfo(sender, stamp));
        this.add(makeFileInfoPane(fileName, fileSize));
        this.add(downloadPanel);

    }

    private JTextPane makeFileInfoPane(String fileName, long fileSize) {
        JTextPane pane = new JTextPane();
        pane.setEditable(false);

        try {
            StyledDocument document = pane.getStyledDocument();
            SimpleAttributeSet attributeSet = new SimpleAttributeSet();
            StyleConstants.setFontSize(attributeSet, 14);
            StyleConstants.setForeground(attributeSet, Color.BLACK);
            document.insertString(0, fileName, attributeSet);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        try {
            StyledDocument document = pane.getStyledDocument();
            SimpleAttributeSet attributeSet = new SimpleAttributeSet();
            StyleConstants.setFontSize(attributeSet, 14);
            StyleConstants.setForeground(attributeSet, Color.GRAY);
            document.insertString(document.getLength(), String.format(" (%s)", FileUtil.sizeToString(fileSize)), attributeSet);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return pane;
    }

    private DownloadPanel makeDownloadPanel(String fileName, UUID fileId) {
        return new DownloadPanel(handler, this, fileName, fileId);
    }

    @Override
    public void setProgress(long downloadedSize) {
        downloadPanel.setInfo(String.format("Downloading: %s/%s", FileUtil.sizeToString(downloadedSize), FileUtil.sizeToString(fileSize)));
    }

    @Override
    public void onTransferStart() {
        downloadPanel.setInfo("Starting to download...");
    }

    @Override
    public void onTransferSucceed(File file) {
        downloadPanel.setAfterDownload(file);
    }

    @Override
    public void onTransferFailed(String reason) {
        downloadPanel.setBeforeDownload();
        downloadPanel.setInfo("Download failed: " + reason);
    }

}
