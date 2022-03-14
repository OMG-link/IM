package PCGUI.components;

import IM.Client;
import PCGUI.helper.PanelUtil;
import mutils.file.FileObject;
import mutils.file.NoSuchFileIdException;
import protocol.dataPack.FileTransferType;
import protocol.fileTransfer.ClientFileReceiveTask;
import protocol.fileTransfer.IDownloadCallback;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

public class ChatImagePanel extends JPanel {
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
        return new IDownloadCallback() {
            @Override
            public void onSucceed(ClientFileReceiveTask task) {
                FileObject imageFileObject;
                try{
                    imageFileObject = handler.getFileManager().openFile(task.getReceiverFileId());
                }catch (NoSuchFileIdException e){
                    throw new RuntimeException("Image file not found!",e);
                }
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
            public void onFailed(ClientFileReceiveTask task,String reason) {
                add(PanelUtil.makeTextArea(Color.RED,22,"[Image] Image download failed: "+reason));
                add(getRetryButton());
            }

            private JButton getRetryButton(){
                JButton button = new JButton("RETRY");
                button.addActionListener(e -> {
                    handler.downloadFile(serverFileId.toString(), serverFileId, FileTransferType.ChatImage,null,getDownloadCallback());
                    remove(2);
                    remove(1);
                });
                return button;
            }

        };
    }

}
