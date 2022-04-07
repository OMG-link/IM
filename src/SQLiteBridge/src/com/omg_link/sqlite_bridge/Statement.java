package com.omg_link.sqlite_bridge;

import java.sql.SQLException;

public class Statement extends com.omg_link.im.core.sql_manager.components.Statement {

    private final java.sql.Statement statement;

    public Statement(java.sql.Statement statement) {
        this.statement = statement;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return new ResultSet(statement.executeQuery(sql));
    }

    @Override
    public void executeUpdate(String sql) throws SQLException {
        statement.executeUpdate(sql);
    }

    @Override
    public void close() throws SQLException {
        statement.close();
    }
}
