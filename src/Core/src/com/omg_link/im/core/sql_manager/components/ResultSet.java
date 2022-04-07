package com.omg_link.im.core.sql_manager.components;

import java.sql.SQLException;

public abstract class ResultSet {

    public abstract boolean next() throws SQLException;

    public abstract String getString(String columnName) throws SQLException;
    public abstract byte[] getBytes(String columnName) throws SQLException;
    public abstract long getLong(String columnName) throws SQLException;

}
