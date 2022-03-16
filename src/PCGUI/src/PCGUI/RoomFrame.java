package PCGUI;

import PCGUI.components.*;
import im.Client;
import im.config.Config;
import im.gui.IFileTransferringPanel;
import im.gui.IRoomFrame;
import im.protocol.fileTransfer.IDownloadCallback;
import im.user_manager.User;
import mutils.IStringGetter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.UUID;

public class RoomFrame extends JFrame implements IRoomFrame, IInputCallback {
    private final Client client;

    private final GridBagLayout gridBagLayout = new GridBagLayout();
    private final GridBagConstraints gridBagConstraints = new GridBagConstraints();
    private MessageArea messageArea;
    private JTextArea userListArea;
    private ChatInputArea inputArea;
    private JPanel buttonPanel;

    public RoomFrame(Client client) {
        this.client = client;

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
        if (client.sendChat(this.inputArea.getText())) {
            this.inputArea.setText("");
        }
    }

    private void appendComponent(Component component, int gridX, int gridY, int gridWidth, int gridHeight) {
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

        appendComponent(component, 0, 0, 3, 4);

    }

    private void makeUserListArea() {
        this.userListArea = new JTextArea();
        var component = this.userListArea;
        component.setPreferredSize(new Dimension(200, 450));
        component.setBackground(Color.GRAY);
        component.setFont(Config.getPreferredFont());
        component.setText(String.format("Please wait while connecting to %s:%d.",Config.getServerIP(),Config.getServerPort()));
        component.setEditable(false);
        component.setLineWrap(true);
        component.setWrapStyleWord(true);

        JScrollPane pane = new JScrollPane(component, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        appendComponent(pane, 3, 0, 1, 4);

    }

    private void makeInputArea() {
        this.inputArea = new ChatInputArea(this);

        var component = this.inputArea;
        component.setPreferredSize(new Dimension(800, 120));
        component.setBackground(Color.WHITE);
        component.setText("");
        component.setLineWrap(true);
        component.setWrapStyleWord(true);
        component.setEnabled(false);

        JScrollPane pane = new JScrollPane(component, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        appendComponent(pane, 0, 4, 4, 1);

    }

    private void makeButtonArea() {
        this.buttonPanel = new JPanel();
        var component = this.buttonPanel;

        //Upload File Button
        JButton uploadFileButton = new JButton("Upload File");
        uploadFileButton.setEnabled(false);
        uploadFileButton.addActionListener((event) -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Upload File");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setMultiSelectionEnabled(true);
            var result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File[] files = fileChooser.getSelectedFiles();
                for (File file : files) {
                    inputArea.uploadFile(file);
                }
            }
        });
        component.add(uploadFileButton);

        //Send Button
        JButton sendButton = new JButton("Send Chat");
        sendButton.setEnabled(false);
        sendButton.addActionListener((event) -> this.onInputFinish());
        component.add(sendButton);

//        //Debug Button
//        JButton debugButton = new JButton("Debug");
//        debugButton.addActionListener((event)->{
//            handler.getNetworkHandler().interrupt();
//        });
//        component.add(debugButton);

        appendComponent(component, 0, 5, 4, 1);

    }

    private void updateUserList(){
        updateUserList(client.getUserManager().getUserList());
    }

    @Override
    public void updateUserList(Collection<User> userList) {
        StringBuilder text = new StringBuilder();
        for (User user : userList) {
            text.append(user.getName()).append('\n');
        }
        this.userListArea.setText(text.toString());
    }

    @Override
    public void onConnectionBuilt() {
        //Clear message area
        clearMessageArea();
        //Enable buttons
        inputArea.setEnabled(true);
        for(Component component: buttonPanel.getComponents()){
            if(component instanceof JButton){
                component.setEnabled(true);
            }
        }
        //Fetch data from user list
        updateUserList();
    }

    @Override
    public void showSystemMessage(String message) {
        showTextMessage("System",System.currentTimeMillis(),message);
    }

    private void clearMessageArea() {
        this.messageArea.clearMessageArea();
    }

    @Override
    public void showTextMessage(String sender, long stamp, String text) {
        this.messageArea.add(new TextPanel(stamp, sender, text));
    }

    @Override
    public IDownloadCallback showChatImageMessage(String sender, long stamp, UUID serverFileId) {
        var panel = new ChatImagePanel(client, sender, stamp, serverFileId);
        this.messageArea.add(panel);
        return panel.getDownloadCallback();
    }

    @Override
    public void onRoomNameUpdate(String roomName) {
        if(roomName.length()>0){
            setTitle(String.format("%s | IM - Made by OMG_link",roomName));
        }
    }

    @Override
    public void showFileUploadedMessage(String sender, long stamp, UUID uuid, String fileName, long fileSize) {
        this.messageArea.add(new FilePanel(client, sender, stamp, uuid, fileName, fileSize));
    }

    @Override
    public IFileTransferringPanel addFileTransferringPanel(IStringGetter fileNameGetter,long fileSize) {
        UploadPanel panel = new UploadPanel(fileNameGetter,fileSize);
        this.messageArea.add(panel);
        return panel;
    }

    @Override
    public void onUserJoined(User user) {
        updateUserList();
        showSystemMessage(String.format("%s joined the chatroom",user.getName()));
    }

    @Override
    public void onUserLeft(User user) {
        updateUserList();
        showSystemMessage(String.format("%s left the chatroom",user.getName()));
    }

    @Override
    public void onUsernameChanged(User user,String previousName) {
        updateUserList();
    }

    public Client getClient() {
        return client;
    }

}
