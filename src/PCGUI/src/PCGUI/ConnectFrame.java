package PCGUI;

import im.config.InvalidUserNameException;
import im.gui.IConnectFrame;
import im.Client;
import im.config.Config;
import PCGUI.components.IInputCallback;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class ConnectFrame extends JFrame implements IConnectFrame,IInputCallback {
    private final Client client;

    private final GridBagLayout gridBagLayout = new GridBagLayout();
    private final GridBagConstraints gridBagConstraints = new GridBagConstraints();

    private JTextArea urlInputArea,nameInputArea;
    private JCheckBox runServerCheckBox;
    private JButton submitButton;

    public ConnectFrame(Client client){
        this.client = client;

        this.setTitle("Set Server");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        this.setLayout(gridBagLayout);
        this.gridBagConstraints.anchor = GridBagConstraints.WEST;
        //this.gridBagConstraints.fill = GridBagConstraints.BOTH;

        this.makeDesc(0,0,"Server IP: ");
        this.makeUrlInputArea();
        this.makeDesc(0,1,"Your name: ");
        this.makeNameInputArea();
        this.makeRunServerCheckBox();
        this.makeSubmitButton();

        this.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(screenSize.width/2-this.getWidth()/2,screenSize.height/2-this.getHeight()/2);

    }

    public void onInputFinish(){
        try{
            if(this.client.setConfigAndStart(
                    this.urlInputArea.getText(),
                    this.nameInputArea.getText(),
                    this.runServerCheckBox.isSelected())
            ){
                this.dispose();
            }
        }catch (InvalidUserNameException e){
            client.showInfo("Your name should be no more than 20 characters.");
        }
    }

    private void makeDesc(int gridX,int gridY,String text){
        JLabel label = new JLabel(text);

        this.gridBagConstraints.anchor = GridBagConstraints.CENTER;
        this.gridBagConstraints.gridx = gridX;
        this.gridBagConstraints.gridy = gridY;
        this.gridBagConstraints.gridwidth = 1;
        this.gridBagConstraints.gridheight = 1;
        this.gridBagLayout.setConstraints(label,this.gridBagConstraints);

        this.add(label);

    }

    private void makeUrlInputArea(){
        this.urlInputArea = new JTextArea(1,30){
            @Override
            protected void processKeyEvent(KeyEvent e){
                if(e.getKeyCode()==KeyEvent.VK_ENTER) return;
                super.processKeyEvent(e);
            }
        };

        this.urlInputArea.setText(Config.getUrl());
        this.urlInputArea.setBackground(Color.LIGHT_GRAY);
        this.urlInputArea.setFont(Config.getPreferredFont());
        this.urlInputArea.requestFocus();
        this.urlInputArea.setCaretPosition(this.urlInputArea.getText().length());

        this.gridBagConstraints.anchor = GridBagConstraints.CENTER;
        this.gridBagConstraints.gridx = 1;
        this.gridBagConstraints.gridy = 0;
        this.gridBagConstraints.gridwidth = 2;
        this.gridBagConstraints.gridheight = 1;
        this.gridBagLayout.setConstraints(this.urlInputArea,this.gridBagConstraints);

        this.add(this.urlInputArea);

    }

    private void makeNameInputArea(){
        this.nameInputArea = new JTextArea(1,30){
            @Override
            protected void processKeyEvent(KeyEvent e){
                if(e.getKeyCode()==KeyEvent.VK_ENTER) return;
                super.processKeyEvent(e);
            }
        };

        this.nameInputArea.setText(Config.getUsername());
        this.nameInputArea.setBackground(Color.LIGHT_GRAY);
        this.nameInputArea.setFont(Config.getPreferredFont());
        this.nameInputArea.setCaretPosition(this.nameInputArea.getText().length());

        this.gridBagConstraints.anchor = GridBagConstraints.CENTER;
        this.gridBagConstraints.gridx = 1;
        this.gridBagConstraints.gridy = 1;
        this.gridBagConstraints.gridwidth = 2;
        this.gridBagConstraints.gridheight = 1;
        this.gridBagLayout.setConstraints(this.nameInputArea,this.gridBagConstraints);

        this.add(this.nameInputArea);

    }

    private void makeRunServerCheckBox(){
        this.runServerCheckBox = new JCheckBox("Start Local Server");
        //this.runServerCheckBox.setSelected(true);

        this.gridBagConstraints.anchor = GridBagConstraints.WEST;
        this.gridBagConstraints.gridx = 0;
        this.gridBagConstraints.gridy = 2;
        this.gridBagConstraints.gridwidth = 3;
        this.gridBagConstraints.gridheight = 1;
        this.gridBagLayout.setConstraints(this.runServerCheckBox,this.gridBagConstraints);

        this.add(this.runServerCheckBox);

    }

    private void makeSubmitButton(){
        this.submitButton = new JButton("OK");
        this.submitButton.addActionListener(new SubmitButtonListener(this));

        this.gridBagConstraints.anchor = GridBagConstraints.CENTER;
        this.gridBagConstraints.gridx = 0;
        this.gridBagConstraints.gridy = 3;
        this.gridBagConstraints.gridwidth = 3;
        this.gridBagConstraints.gridheight = 1;
        this.gridBagLayout.setConstraints(this.submitButton,this.gridBagConstraints);

        this.add(this.submitButton);

    }

    private static class SubmitButtonListener implements ActionListener{
        final ConnectFrame handler;

        SubmitButtonListener(ConnectFrame handler){
            this.handler = handler;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            handler.onInputFinish();
        }

    }

}
