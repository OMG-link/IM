package com.omg_link.im.pc_gui.components;

import com.omg_link.im.config.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class MessageArea {
    private final JPanel panel;
    private Box box;
    private JScrollBar bar;

    public MessageArea(Dimension dimension){
        this.panel = new JPanel();
        var component = this.panel;
        component.setPreferredSize(dimension);
        component.setLayout(new BorderLayout());

        this.makeMessageBox();

    }

    private void makeMessageBox(){
        this.box = Box.createVerticalBox();
        var component = this.box;

        JScrollPane pane = new JScrollPane(component,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setPreferredSize(this.panel.getPreferredSize());

        configureBar(pane);

        this.panel.add(pane,BorderLayout.WEST);

    }

    private void configureBar(JScrollPane pane) {
        this.bar = pane.getVerticalScrollBar();
        this.bar.setUnitIncrement(Config.getPreferredFont().getSize());
        this.bar.addAdjustmentListener(new AdjustmentListener() {
            private int lastMaximum = 0;
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if(bar.getMaximum()!=this.lastMaximum){
                    this.lastMaximum = bar.getMaximum();
                    bar.setValue(bar.getMaximum());
                }
            }
        });
    }

    public JPanel getPanel() {
        return panel;
    }

    public void clearMessageArea(){
        box.removeAll();
        this.box.revalidate();
        this.box.repaint();
    }

    public void add(Component component){
        this.box.add(component);
        this.box.revalidate();
        this.box.repaint();
    }

}
