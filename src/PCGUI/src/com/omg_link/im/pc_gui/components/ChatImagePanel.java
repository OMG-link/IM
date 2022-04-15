package com.omg_link.im.pc_gui.components;

import com.omg_link.im.core.ClientRoom;
import com.omg_link.im.core.file_manager.FileObject;
import com.omg_link.im.core.gui.IFileTransferringPanel;
import com.omg_link.im.core.protocol.file_transfer.FileTransferType;
import com.omg_link.im.pc_gui.helper.PanelUtil;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

public class ChatImagePanel extends JPanel implements IFileTransferringPanel {
    private final ClientRoom room;
    private final UUID serverFileId;

    public ChatImagePanel(ClientRoom room, String sender, long stamp, UUID serverFIleId){
        super();

        this.room = room;
        this.serverFileId = serverFIleId;
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.add(PanelUtil.makeMessageInfo(sender,stamp));

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
    public void onTransferFailed(String state) {
        add(PanelUtil.makeTextArea(Color.RED,22,"[Image] Image download failed: "+state));
        add(getRetryButton());
    }

    private JButton getRetryButton(){
        JButton button = new JButton("RETRY");
        button.addActionListener(e -> {
            room.downloadFile(serverFileId.toString(), serverFileId, FileTransferType.ChatImage,this);
            remove(2);
            remove(1);
        });
        return button;
    }

}
