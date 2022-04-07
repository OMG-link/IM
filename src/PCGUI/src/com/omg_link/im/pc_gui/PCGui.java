package com.omg_link.im.pc_gui;

import com.omg_link.im.core.Client;
import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.gui.IConfirmDialogCallback;
import com.omg_link.im.core.gui.IGui;
import com.omg_link.sqlite_bridge.SqlComponentFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class PCGui implements IGui {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            Config.updateFromFile();
            new PCGui();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Client client;

    public PCGui() {
        try{
            Class.forName("org.sqlite.JDBC");
            client = new Client(this);
            client.getGui().createConnectFrame();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createConnectFrame() {
        ConnectFrame connectFrame = new ConnectFrame(client);
        connectFrame.setVisible(true);
        client.setConnectFrame(connectFrame);
    }

    @Override
    public void createRoomFrame() {
        RoomFrame roomFrame = new RoomFrame(client.getRoom());
        roomFrame.setVisible(true);
        client.setRoomFrame(roomFrame);
    }

    @Override
    public void showMessageDialog(String message) {
        new Thread(()-> JOptionPane.showMessageDialog(null, message)).start();
    }

    @Override
    public void showConfirmDialog(String message, IConfirmDialogCallback callback) {
        new Thread(()->{
            if (JOptionPane.showConfirmDialog(null, message, "", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                callback.onPositiveInput();
            } else {
                callback.onNegativeInput();
            }
        }).start();
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
    public void alertVersionUnrecognizable(String clientVersion) {
        client.showCheckbox(
                String.format("Unable to read the version of the server, and the program will end shortly.\nYour client runs on version %s\nDo you want to download a new version?", clientVersion),
                new IConfirmDialogCallback() {
                    @Override
                    public void onPositiveInput() {
                        openInBrowser("https://www.omg-link.com/IM/");
                        System.exit(0);
                    }

                    @Override
                    public void onNegativeInput() {
                        System.exit(0);
                    }
                }
        );
    }

    @Override
    public void alertVersionMismatch(String serverVersion, String clientVersion) {
        client.showCheckbox(
                String.format("The server runs on version \"%s\", while your client runs on version \"%s\".\n Do you want to download a new version?", serverVersion, clientVersion),
                new IConfirmDialogCallback() {
                    @Override
                    public void onPositiveInput() {
                        openInBrowser("https://www.omg-link.com/IM/");
                    }

                    @Override
                    public void onNegativeInput() {

                    }
                }
        );
    }

    @Override
    public void alertVersionIncompatible(String serverVersion, String clientVersion) {
        client.showCheckbox(
                String.format("The server runs on version \"%s\", while your client runs on version \"%s\".\nThe two versions are not compatible with each other.\nDo you want to download a new version?", serverVersion, clientVersion),
                new IConfirmDialogCallback() {
                    @Override
                    public void onPositiveInput() {
                        openInBrowser("https://www.omg-link.com/IM/");
                        System.exit(0);
                    }

                    @Override
                    public void onNegativeInput() {
                        System.exit(0);
                    }
                }
        );
    }

    @Override
    public SqlComponentFactory getSqlComponentFactory() {
        return new SqlComponentFactory();
    }

}
