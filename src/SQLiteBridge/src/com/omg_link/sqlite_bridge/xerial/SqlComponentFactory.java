package com.omg_link.sqlite_bridge.xerial;

import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlComponentFactory extends com.omg_link.sqlite_bridge.SqlComponentFactory {

    @Override
    public Connection createConnection(String fileName) throws SQLException {
        return new Connection(DriverManager.getConnection("jdbc:sqlite:"+fileName));
    }

}
