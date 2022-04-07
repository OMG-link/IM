package com.omg_link.im.core.sql_manager.components;

import java.sql.SQLException;

public abstract class Statement implements AutoCloseable {

    public abstract ResultSet executeQuery(String sql) throws SQLException;
    public abstract void executeUpdate(String sql) throws SQLException;

    public abstract void close() throws SQLException;

}
