package com.omg_link.sqlite_bridge;

import java.sql.SQLException;

public abstract class PreparedStatement {

    public abstract void setLong(int index,long value) throws SQLException;
    public abstract void setBytes(int index,byte[] value) throws SQLException;
    public abstract void setString(int index,String value) throws SQLException;

    public abstract void executeUpdate() throws SQLException;
    public abstract Cursor executeQuery() throws SQLException;

}
