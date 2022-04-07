package com.omg_link.sqlite_bridge;

import java.sql.SQLException;

public abstract class SqlComponentFactory {

    public abstract Connection createConnection(String fileName) throws SQLException;

}
