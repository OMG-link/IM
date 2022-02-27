package IM;

import GUI.IConfirmDialogCallback;
import GUI.IGUI;
import GUI.IRoomFrame;
import mutil.file.ClientFileManager;
import mutil.uuidLocator.UUIDManager;
import protocol.ClientNetworkHandler;
import protocol.dataPack.NameUpdatePack;
import protocol.dataPack.TextPack;
import protocol.helper.data.PackageTooLargeException;
import protocol.helper.fileTransfer.ClientFileSendTask;
import protocol.helper.fileTransfer.FileSendTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client{
    private UUIDManager uuidManager;
    private ClientFileManager fileManager;
    private ClientNetworkHandler networkHandler;
    private IRoomFrame roomFrame;
    private IGUI GUI;

    public Client(IGUI GUI){
        try{
            this.GUI = GUI;
            this.uuidManager = new UUIDManager();
            this.fileManager = new ClientFileManager();
        }catch (Exception e){
            Logger.getGlobal().log(Level.SEVERE,"Unable to create server instance: "+e);
            System.exit(0);
        }
    }

    public void start(){
        getGUI().createConnectFrame();
    }

    public void runNetworkHandler(){
        if(this.networkHandler!=null){
            this.networkHandler.interrupt();
        }
        this.networkHandler = new ClientNetworkHandler(this, Config.getServerIP(), Config.getServerPort());
        this.networkHandler.connect();
        if(this.getRoomFrame()!=null){
            this.getRoomFrame().clearMessageArea();
        }
        try{
            this.networkHandler.send(new NameUpdatePack(Config.getUsername()));
        }catch (PackageTooLargeException e){
            throw new RuntimeException(e);
        }
    }

    public void createRoomFrame(){
        getGUI().createRoomFrame();
    }

    public boolean sendChat(String message){
        if(message.length()==0) return false;
        TextPack textPack = new TextPack(message);

        try{
            getNetworkHandler().send(textPack);
        }catch (PackageTooLargeException e){
            showInfo("Your message is too long!");
            return false;
        }
        return true;
    }

    public void uploadFile(File file) throws FileNotFoundException {
        FileSendTask task = new ClientFileSendTask(this,file);
        task.start();
    }

    public boolean setConfigAndStart(String url,String username,boolean shouldRunLocalServer){
        //config
        try{
            Config.setUrl(url);
            Config.setUsername(username);
            Config.saveToFile();
        }catch (ConfigSetFailedException e){
            this.showInfo(e.getMessage());
            return false;
        }
        //server
        if(shouldRunLocalServer){
            Config.setServerIP("127.0.0.1");
            Server server = new Server(null);
            server.start();
        }
        //connect
        createRoomFrame();
        runNetworkHandler();
        return true;
    }

    public void showInfo(String message){
        getGUI().showMessageDialog(message);
    }

    public void showCheckBox(String message, IConfirmDialogCallback callback){
        getGUI().showConfirmDialog(message,callback);
    }

    public void showException(Exception e){
        getGUI().showException(e);
    }

    public ClientNetworkHandler getNetworkHandler() {
        return networkHandler;
    }

    public IRoomFrame getRoomFrame() {
        return roomFrame;
    }

    public void setRoomFrame(IRoomFrame roomFrame){
        this.roomFrame = roomFrame;
    }

    public UUIDManager getUuidManager() {
        return uuidManager;
    }

    public ClientFileManager getFileManager() {
        return fileManager;
    }

    public IGUI getGUI() {
        return GUI;
    }
}
