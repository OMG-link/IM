package com.omg_link.im.core.sql_manager.components;

import java.sql.SQLException;

public abstract class SqlComponentFactory {

    public abstract Connection createConnection(String fileName) throws SQLException;

}
