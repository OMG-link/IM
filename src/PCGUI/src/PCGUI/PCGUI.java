package PCGUI;

import GUI.IConfirmDialogCallback;
import GUI.IConnectFrame;
import GUI.IGUI;
import GUI.IRoomFrame;
import IM.Client;
import IM.Config;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class PCGUI implements IGUI {
    public static void main(String[] args) {
        try {
            Config.updateFromFile();
            PCGUI GUI = new PCGUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Client client;

    public PCGUI() {
        client = new Client(this);
        client.start();
    }

    @Override
    public void createConnectFrame() {
        IConnectFrame connectFrame = new ConnectFrame(client);
        connectFrame.setVisible(true);
    }

    @Override
    public void createRoomFrame() {
        IRoomFrame roomFrame = new RoomFrame(client);
        roomFrame.setVisible(true);
        client.setRoomFrame(roomFrame);
    }

    @Override
    public void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    @Override
    public void showConfirmDialog(String message, IConfirmDialogCallback callback) {
        if (JOptionPane.showConfirmDialog(null, message, "", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            callback.onPositiveInput();
        } else {
            callback.onNegativeInput();
        }
    }

    @Override
    public void showException(Exception e) {
        e.printStackTrace();
        showMessageDialog(e.toString());
    }

    @Override
    public void openInBrowser(String uri) {
        try {
            Desktop.getDesktop().browse(new URI(uri));
        } catch (IOException | URISyntaxException ignored) {
        }
    }

    @Override
    public void alertVersionError(String serverVersion, String clientVersion) {
        client.showCheckBox(
                String.format("The server runs on version \"%s\", while your client runs on version \"%s\".\n Do you want to download new version?", serverVersion, clientVersion),
                new IConfirmDialogCallback() {
                    @Override
                    public void onPositiveInput() {
                        openInBrowser("https://www.omg-link.com:8888/IM/");
                    }

                    @Override
                    public void onNegativeInput() {

                    }
                }
        );
    }
}
