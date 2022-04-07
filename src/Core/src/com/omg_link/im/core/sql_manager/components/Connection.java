package com.omg_link.im.core.sql_manager.components;

import java.sql.SQLException;

public abstract class Connection {

    public abstract Statement createStatement() throws SQLException;
    public abstract PreparedStatement prepareStatement(String sql) throws SQLException;

    public abstract void close() throws SQLException;

}
