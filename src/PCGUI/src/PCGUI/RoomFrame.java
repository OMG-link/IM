package PCGUI;

import GUI.IFileTransferringPanel;
import GUI.IRoomFrame;
import IM.Client;
import IM.Config;
import PCGUI.components.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.UUID;

public class RoomFrame extends JFrame implements IRoomFrame,IInputCallback {
    private final Client handler;

    private final GridBagLayout gridBagLayout = new GridBagLayout();
    private final GridBagConstraints gridBagConstraints = new GridBagConstraints();
    private MessageArea messageArea;
    private JTextArea userListArea, inputArea;
    private JPanel buttonPanel;

    public RoomFrame(Client handler) {
        this.handler = handler;

        this.setTitle("IM - Made by OMG_link");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(gridBagLayout);
        this.setResizable(false);

        this.gridBagConstraints.fill = GridBagConstraints.BOTH;

        makeMessageArea();
        makeUserListArea();
        makeInputArea();
        makeButtonArea();

        this.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(screenSize.width / 2 - this.getWidth() / 2, screenSize.height / 2 - this.getHeight() / 2);

    }

    public void onInputFinish() {
        if(handler.sendChat(this.inputArea.getText())){
            this.inputArea.setText("");
        }
    }

    private void appendComponent(Component component,int gridX,int gridY,int gridWidth,int gridHeight) {
        this.gridBagConstraints.gridx = gridX;
        this.gridBagConstraints.gridy = gridY;
        this.gridBagConstraints.gridwidth = gridWidth;
        this.gridBagConstraints.gridheight = gridHeight;
        this.gridBagLayout.setConstraints(component, this.gridBagConstraints);

        this.add(component);

    }

    private void makeMessageArea() {
        this.messageArea = new MessageArea(new Dimension(600, 450));
        var component = this.messageArea.getPanel();

        appendComponent(component,0,0,3,4);

    }

    private void makeUserListArea() {
        this.userListArea = new JTextArea();
        var component = this.userListArea;
        component.setPreferredSize(new Dimension(200, 450));
        component.setBackground(Color.GRAY);
        component.setFont(Config.getPreferredFont());
        component.setText(String.format("User list is being developed.\nYour name is \"%s\".\n", Config.getUsername()));
        component.setEditable(false);
        component.setLineWrap(true);
        component.setWrapStyleWord(true);

        JScrollPane pane = new JScrollPane(component, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        appendComponent(pane,3,0,1,4);

    }

    private void makeInputArea() {
        this.inputArea = new ChatInputArea(this);

        var component = this.inputArea;
        component.setPreferredSize(new Dimension(800, 120));
        component.setBackground(Color.WHITE);
        component.setText("");
        component.setLineWrap(true);
        component.setWrapStyleWord(true);

        JScrollPane pane = new JScrollPane(component, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        appendComponent(pane,0,4,4,1);

    }

    private void makeButtonArea(){
        this.buttonPanel = new JPanel();
        var component = this.buttonPanel;

        //Upload File Button
        JButton uploadFileButton = new JButton("Upload File");
        uploadFileButton.addActionListener((event)->{
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Upload File");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setMultiSelectionEnabled(true);
            var result = fileChooser.showOpenDialog(this);
            if(result==JFileChooser.APPROVE_OPTION){
                File[] files = fileChooser.getSelectedFiles();
                for(File file:files){
                    try{
                        handler.uploadFile(file);
                    }catch (FileNotFoundException ignored){}
                }
            }
        });
        component.add(uploadFileButton);

        //Send Button
        JButton sendButton = new JButton("Send Chat");
        sendButton.addActionListener((event)->{
            this.onInputFinish();
        });
        component.add(sendButton);

//        //Debug Button
//        JButton debugButton = new JButton("Debug");
//        debugButton.addActionListener((event)->{
//            handler.getNetworkHandler().interrupt();
//        });
//        component.add(debugButton);

        appendComponent(component,0,5,4,1);

    }

    public void clearMessageArea(){
        this.messageArea.clearMessageArea();
    }

    public void onMessageReceive(String sender, long stamp, String text) {
        this.messageArea.add(new TextPanel(stamp, sender, text));
    }

    public void onUserListUpdate(String[] userList) {
        StringBuilder text = new StringBuilder();
        for (String s : userList) {
            text.append(s).append('\n');
        }
        this.userListArea.setText(text.toString());
    }

    public void onFileUploadedReceive(String sender, long stamp, UUID uuid, String fileName, long fileSize) {
        this.messageArea.add(new FilePanel(handler, sender, stamp, uuid, fileName, fileSize));
    }

    public IFileTransferringPanel addFileTransferringPanel(String fileName) {
        FileTransferringPanel panel = new FileTransferringPanel(fileName);
        this.messageArea.add(panel);
        return panel;
    }

    public Client getHandler(){
        return handler;
    }

}
