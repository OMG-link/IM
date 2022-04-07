package com.omg_link.sqlite_bridge;

import java.sql.SQLException;

public abstract class Connection {

    public abstract Statement createStatement() throws SQLException;
    public abstract PreparedStatement prepareStatement(String sql) throws SQLException;

    public abstract void close() throws SQLException;

}
