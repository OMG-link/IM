package PCGUI.components;

import PCGUI.helper.PanelUtil;
import IM.Client;
import protocol.dataPack.DownloadRequestPack;
import protocol.helper.data.PackageTooLargeException;

import javax.swing.*;
import javax.swing.text.*;
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
        downloadButton.addActionListener(event -> {
            try{
                handler.getNetworkHandler().send(new DownloadRequestPack(uuid));
            }catch (PackageTooLargeException e){
                //This should never happen!
                handler.showInfo("Unable to send download request.");
            }
        });
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
