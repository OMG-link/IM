package com.omg_link.im.pc_gui.components;

import com.omg_link.im.pc_gui.helper.PanelUtil;
import com.omg_link.im.Client;
import com.omg_link.im.file_manager.FileObject;
import com.omg_link.im.gui.IFileTransferringPanel;
import com.omg_link.im.protocol.data_pack.file_transfer.FileTransferType;
import com.omg_link.im.protocol.file_transfer.IDownloadCallback;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

public class ChatImagePanel extends JPanel implements IFileTransferringPanel {
    private final Client handler;
    private final UUID serverFileId;

    public ChatImagePanel(Client handler, String sender, long stamp, UUID serverFIleId){
        super();

        this.handler = handler;
        this.serverFileId = serverFIleId;
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.add(PanelUtil.makeMessageInfo(sender,stamp));

    }

    public IDownloadCallback getDownloadCallback(){
        return null;
    }

    @Override
    public void setProgress(long downloadedSize) {

    }

    @Override
    public void onTransferStart() {

    }

    @Override
    public void onTransferSucceed(FileObject imageFileObject) {
        var imagePath = imageFileObject.getFile().getAbsolutePath();
        var icon = new ImageIcon(imagePath);
        if(icon.getIconHeight()<=0){
            add(PanelUtil.makeTextArea(Color.RED,22,"[Image] Unable to resolve image."));
        }else{
            add(new ImagePanel(icon));
        }
        revalidate();
        repaint();
    }

    @Override
    public void onTransferFailed(String reason) {
        add(PanelUtil.makeTextArea(Color.RED,22,"[Image] Image download failed: "+reason));
        add(getRetryButton());
    }

    private JButton getRetryButton(){
        JButton button = new JButton("RETRY");
        button.addActionListener(e -> {
            handler.downloadFile(serverFileId.toString(), serverFileId, FileTransferType.ChatImage,this);
            remove(2);
            remove(1);
        });
        return button;
    }

}
