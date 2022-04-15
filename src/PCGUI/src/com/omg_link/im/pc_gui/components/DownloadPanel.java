package com.omg_link.im.pc_gui.components;

import com.omg_link.im.core.ClientRoom;
import com.omg_link.im.core.file_manager.FileObject;
import com.omg_link.im.core.gui.IFileTransferringPanel;
import com.omg_link.im.core.protocol.file_transfer.FileTransferType;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.UUID;

public class DownloadPanel extends JPanel{
    private final ClientRoom room;
    private final IFileTransferringPanel transferringPanel;
    private final String fileName;
    private final UUID serverFileId;

    private final JPanel buttonPanel = new JPanel();
    private final JTextArea textArea;

    public DownloadPanel(ClientRoom room, IFileTransferringPanel transferringPanel, String fileName, UUID serverFileId){
        super();
        this.room = room;
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
            room.downloadFile(fileName, serverFileId, FileTransferType.ChatFile,transferringPanel);
        });
        buttonPanel.add(downloadButton);

        buttonPanel.revalidate();
        buttonPanel.repaint();

    }

    public void setAfterDownload(FileObject fileObject){
        buttonPanel.removeAll();

        var downloadedFile = fileObject.getFile();
        textArea.setText(String.format("File downloaded to: %s",downloadedFile.getAbsolutePath()));

        JButton openFileButton = new JButton("OPEN");
        openFileButton.addActionListener(event -> {
            if(!Desktop.isDesktopSupported()){
                room.showMessage("Your JVM does not support this operation.");
                return;
            }
            if(!downloadedFile.exists()){
                room.showMessage("File not exists, try download again.");
                setBeforeDownload();
                return;
            }
            try{
                Desktop.getDesktop().open(downloadedFile);
            }catch (IOException e){
                room.showMessage(e.toString());
                e.printStackTrace();
            }
        });
        buttonPanel.add(openFileButton);

        JButton openInExplorerButton = new JButton("OPEN IN EXPLORER");
        openInExplorerButton.addActionListener(event -> {
            if(!Desktop.isDesktopSupported()){
                room.showMessage("Your JVM does not support this operation.");
                return;
            }
            if(!downloadedFile.exists()){
                room.showMessage("File not exists, try download again.");
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
                room.showMessage(e.toString());
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
