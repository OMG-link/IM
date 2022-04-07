package com.omg_link.sqlite_bridge;

import java.sql.SQLException;

public class ResultSet extends com.omg_link.im.core.sql_manager.components.ResultSet {

    private final java.sql.ResultSet resultSet;

    public ResultSet(java.sql.ResultSet resultSet){
        this.resultSet = resultSet;
    }

    @Override
    public boolean next() throws SQLException {
        return resultSet.next();
    }

    @Override
    public String getString(String columnName) throws SQLException {
        return resultSet.getString(columnName);
    }

    @Override
    public byte[] getBytes(String columnName) throws SQLException {
        return resultSet.getBytes(columnName);
    }

    @Override
    public long getLong(String columnName) throws SQLException {
        return resultSet.getLong(columnName);
    }

}
