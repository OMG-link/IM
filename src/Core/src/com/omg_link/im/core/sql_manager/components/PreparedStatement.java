package com.omg_link.im.core.sql_manager.components;

import java.sql.SQLException;

public abstract class PreparedStatement {

    public abstract void setLong(int index,long value) throws SQLException;
    public abstract void setBytes(int index,byte[] value) throws SQLException;
    public abstract void setString(int index,String value) throws SQLException;

    public abstract void executeUpdate() throws SQLException;
    public abstract ResultSet executeQuery() throws SQLException;

}
