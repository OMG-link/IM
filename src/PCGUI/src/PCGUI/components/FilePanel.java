package PCGUI.components;

import IM.Client;
import PCGUI.helper.PanelUtil;
import protocol.dataPack.FileTransferType;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.UUID;

public class FilePanel extends JPanel {
    private final Client handler;

    public FilePanel(Client handler,String sender, long stamp, UUID uuid, String fileName, long fileSize){
        super();
        this.handler = handler;

        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.add(PanelUtil.makeMessageInfo(sender,stamp));
        this.add(makeFileInfoPane(fileName,fileSize));
        this.add(makeDownloadPanel(uuid));

    }

    private JTextPane makeFileInfoPane(String fileName,long fileSize){
        JTextPane pane = new JTextPane();
        pane.setEditable(false);

        try{
            StyledDocument document = pane.getStyledDocument();
            SimpleAttributeSet attributeSet = new SimpleAttributeSet();
            StyleConstants.setFontSize(attributeSet,14);
            StyleConstants.setForeground(attributeSet, Color.BLACK);
            document.insertString(0,fileName,attributeSet);
        }catch (BadLocationException e){
            e.printStackTrace();
        }

        try{
            StyledDocument document = pane.getStyledDocument();
            SimpleAttributeSet attributeSet = new SimpleAttributeSet();
            StyleConstants.setFontSize(attributeSet,14);
            StyleConstants.setForeground(attributeSet, Color.GRAY);
            document.insertString(document.getLength(),String.format(" (%s)",sizeToString(fileSize)),attributeSet);
        }catch (BadLocationException e){
            e.printStackTrace();
        }

        return pane;
    }

    private JPanel makeDownloadPanel(UUID uuid){
        JPanel panel = new JPanel();

        JButton downloadButton = new JButton("Download");
        downloadButton.addActionListener(event -> handler.downloadFile(uuid,FileTransferType.ChatFile));
        panel.add(downloadButton);

        return panel;
    }

    private String sizeToString(long size){
        if(size<1000L){
            return String.format("%dB",size);
        }else if(size<(1L<<10)*1000L){
            return String.format("%.2fKB",(double)size/(1L<<10));
        }else if(size<(1L<<20)*1000L){
            return String.format("%.2fMB",(double)size/(1L<<20));
        }else if(size<(1L<<30)*1000L){
            return String.format("%.2fGB",(double)size/(1L<<30));
        }else{
            return String.format("%.2fTB",(double)size/(1L<<40));
        }
    }

}
