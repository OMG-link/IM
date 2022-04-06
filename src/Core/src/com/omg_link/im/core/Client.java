package com.omg_link.im.core;

import com.omg_link.im.core.config.ConfigSetFailedException;
import com.omg_link.im.core.gui.IConfirmDialogCallback;
import com.omg_link.im.core.gui.IConnectFrame;
import com.omg_link.im.core.gui.IGui;
import com.omg_link.im.core.gui.IRoomFrame;

import java.util.logging.Logger;

public class Client {
    private final Logger logger = Logger.getLogger("IMClient");

    private final IGui gui;

    private ClientRoom room = null;
    private ServerRoom serverRoom = null;

    /**
     * Create a client.
     */
    public Client(IGui gui) {
        this.gui = gui;
    }

    /**
     * Get whether the local server running.
     * @return True if the local server is running.
     */
    public boolean isLocalServerRunning(){
        return serverRoom != null;
    }

    /**
     * <p>Start a local server.</p>
     * <p>This method will not create a new server if a local server is still running</p>
     */
    public void runLocalServer(){
        if(isLocalServerRunning()) return;
        serverRoom = new ServerRoom(null);
    }

    /**
     * Connect to a room with the following arguments.
     * @param url The url of the server.
     * @param username The username user wants to use.
     * @param token The password for the room.
     * @throws ConfigSetFailedException When any of the argument is invalid.
     */
    public void connectToRoom(String url, String username, String token) throws ConfigSetFailedException {
        if(room!=null){
            room.exitRoom(IRoomFrame.ExitReason.ConnectingToNewRoom);
            room = null;
        }
        room = new ClientRoom(this);
        getGui().createRoomFrame();
        room.saveConfigAndConnect(url,username,token);
    }

    //show messages

    public void showMessage(String message) {
        getGui().showMessageDialog(message);
    }

    public void showCheckbox(String message, IConfirmDialogCallback callback) {
        getGui().showConfirmDialog(message, callback);
    }

    //getter and setter

    public void setConnectFrame(IConnectFrame connectFrame){}

    public void setRoomFrame(IRoomFrame roomFrame){
        getRoom().setRoomFrame(roomFrame);
    }

    public Logger getLogger() {
        return logger;
    }

    public void setRoom(ClientRoom room) {
        this.room = room;
    }

    public ClientRoom getRoom() {
        return room;
    }

    public IGui getGui() {
        return gui;
    }

}
