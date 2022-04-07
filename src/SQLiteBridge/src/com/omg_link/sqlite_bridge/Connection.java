package com.omg_link.sqlite_bridge;

import java.sql.SQLException;

public class Connection extends com.omg_link.im.core.sql_manager.components.Connection {

    private final java.sql.Connection connection;

    public Connection(java.sql.Connection connection){
        this.connection = connection;
    }

    @Override
    public Statement createStatement() throws SQLException {
        return new Statement(connection.createStatement());
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return new PreparedStatement(connection.prepareStatement(sql));
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
