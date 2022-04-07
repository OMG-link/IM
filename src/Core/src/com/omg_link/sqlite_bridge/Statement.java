package com.omg_link.sqlite_bridge;

import java.sql.SQLException;

public abstract class Statement implements AutoCloseable {

    public abstract Cursor executeQuery(String sql) throws SQLException;
    public abstract void executeUpdate(String sql) throws SQLException;

    public abstract void close() throws SQLException;

}
