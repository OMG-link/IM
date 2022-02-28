package PCGUI.components;

import IM.Client;
import PCGUI.helper.PanelUtil;
import protocol.dataPack.ImageType;
import protocol.helper.fileTransfer.ClientFileReceiveTask;
import protocol.helper.fileTransfer.IDownloadCallback;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

public class ChatImagePanel extends JPanel {
    private final Client handler;

    private final UUID imageUuid;
    private final ImageType imageType;

    public ChatImagePanel(Client handler, String sender, long stamp, UUID imageUuid, ImageType imageType){
        super();

        this.handler = handler;
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        this.add(PanelUtil.makeMessageInfo(sender,stamp));
        this.imageUuid = imageUuid;
        this.imageType = imageType;

    }

    public IDownloadCallback getDownloadCallback(){
        return new IDownloadCallback() {
            @Override
            public void onSucceed(ClientFileReceiveTask task) {
                var imagePath = handler.getFileManager().getFile(task.getLocalFileId()).getFile().getAbsolutePath();
                add(new ImagePanel(new ImageIcon(imagePath)));
                revalidate();
                repaint();
            }

            @Override
            public void onFailed(ClientFileReceiveTask task) {
                add(PanelUtil.makeTextArea(Color.BLACK,22,"Image download failed."));
                //todo: click to retry
            }
        };
    }

}
