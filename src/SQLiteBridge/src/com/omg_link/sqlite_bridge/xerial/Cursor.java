package com.omg_link.sqlite_bridge.xerial;

import java.sql.SQLException;

public class Cursor extends com.omg_link.sqlite_bridge.Cursor {

    private final java.sql.ResultSet resultSet;

    public Cursor(java.sql.ResultSet resultSet){
        this.resultSet = resultSet;
    }

    @Override
    public boolean next() throws SQLException {
        return resultSet.next();
    }

    @Override
    public void close() {
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
