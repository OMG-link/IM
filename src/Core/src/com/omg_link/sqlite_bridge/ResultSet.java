package com.omg_link.sqlite_bridge;

import java.sql.SQLException;

public abstract class ResultSet {

    public abstract boolean next() throws SQLException;

    public abstract String getString(String columnName) throws SQLException;
    public abstract byte[] getBytes(String columnName) throws SQLException;
    public abstract long getLong(String columnName) throws SQLException;

}
