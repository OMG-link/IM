package com.omg_link.im.core.gui;

import com.omg_link.im.core.sql_manager.components.SqlComponentFactory;

public interface IServerGui {
    void createGUI();

    /**
     * <p>Get the factory for sql components.</p>
     * <p>I DO NOT WANT TO WRITE THIS AT ALL!!!</p>
     *
     * @return An SQL component factory.
     */
    SqlComponentFactory getSqlComponentFactory();

}
