package PCGUI.components;

import im.gui.IFileTransferringPanel;
import im.Client;
import im.protocol.dataPack.FileTransferType;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DownloadPanel extends JPanel{
    private final Client handler;
    private final IFileTransferringPanel transferringPanel;
    private final String fileName;
    private final UUID serverFileId;

    private final JPanel buttonPanel = new JPanel();
    private final JTextArea textArea;

    public DownloadPanel(Client handler, IFileTransferringPanel transferringPanel, String fileName, UUID serverFileId){
        super();
        this.handler = handler;
        this.transferringPanel = transferringPanel;
        this.fileName = fileName;
        this.serverFileId = serverFileId;

        this.textArea = new JTextArea();
        this.textArea.setEditable(false);

        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.add(textArea);
        this.add(buttonPanel);

    }

    public void setBeforeDownload(){
        buttonPanel.removeAll();

        textArea.setText("");

        JButton downloadButton = new JButton("DOWNLOAD");
        downloadButton.addActionListener(e -> {
            buttonPanel.removeAll();
            handler.downloadFile(fileName, serverFileId, FileTransferType.ChatFile,transferringPanel);
        });
        buttonPanel.add(downloadButton);

        buttonPanel.revalidate();
        buttonPanel.repaint();

    }

    public void setAfterDownload(File downloadedFile){
        buttonPanel.removeAll();

        textArea.setText(String.format("File downloaded to: %s",downloadedFile.getAbsolutePath()));

        JButton openFileButton = new JButton("OPEN");
        openFileButton.addActionListener(event -> {
            if(!Desktop.isDesktopSupported()){
                handler.showInfo("Your JVM does not support this operation.");
                return;
            }
            if(!downloadedFile.exists()){
                handler.showInfo("File not exists, try download again.");
                setBeforeDownload();
                return;
            }
            try{
                Desktop.getDesktop().open(downloadedFile);
            }catch (IOException e){
                handler.showInfo(e.toString());
                e.printStackTrace();
            }
        });
        buttonPanel.add(openFileButton);

        JButton openInExplorerButton = new JButton("OPEN IN EXPLORER");
        openInExplorerButton.addActionListener(event -> {
            if(!Desktop.isDesktopSupported()){
                handler.showInfo("Your JVM does not support this operation.");
                return;
            }
            if(!downloadedFile.exists()){
                handler.showInfo("File not exists, try download again.");
                setBeforeDownload();
                return;
            }
            try{
                if(System.getProperty("os.name").toUpperCase().contains("WINDOWS")){
                    Runtime.getRuntime().exec("explorer /select,"+downloadedFile.getPath());
                }else{
                    Desktop.getDesktop().open(downloadedFile.getParentFile());
                }
            }catch (IOException e){
                handler.showInfo(e.toString());
                e.printStackTrace();
            }
        });
        buttonPanel.add(openInExplorerButton);

        buttonPanel.revalidate();
        buttonPanel.repaint();

    }

    public void setInfo(String info) {
        textArea.setText(info);
    }

}
