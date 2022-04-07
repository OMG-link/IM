package com.omg_link.sqlite_bridge.xerial;

import java.sql.SQLException;

public class PreparedStatement extends com.omg_link.sqlite_bridge.PreparedStatement {

    private final java.sql.PreparedStatement statement;

    public PreparedStatement(java.sql.PreparedStatement statement){
        this.statement = statement;
    }

    @Override
    public void setLong(int index, long value) throws SQLException {
        statement.setLong(index,value);
    }

    @Override
    public void setBytes(int index, byte[] value) throws SQLException {
        statement.setBytes(index,value);
    }

    @Override
    public void setString(int index, String value) throws SQLException {
        statement.setString(index,value);
    }

    @Override
    public void executeUpdate() throws SQLException {
        statement.executeUpdate();
    }

    @Override
    public Cursor executeQuery() throws SQLException {
        return new Cursor(statement.executeQuery());
    }
}
