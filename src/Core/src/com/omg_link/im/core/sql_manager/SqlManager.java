package com.omg_link.im.core.sql_manager;

import java.sql.*;

public abstract class SqlManager {
    protected final Connection connection;

    public SqlManager(String fileName) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
        // Make connection
        connection = DriverManager.getConnection("jdbc:sqlite:" + fileName);
    }

    public void close(){
        try{
            connection.close();
        }catch (SQLException ignored){
        }
    }

    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    /**
     * Check whether the table has been created.
     */
    public boolean isTableCreated(String tableName) throws SQLException {
        try(Statement statement = connection.createStatement()){
            String sql = "PRAGMA table_info("+tableName+")";
            ResultSet resultSet = statement.executeQuery(sql);
            return resultSet.next();
        }
    }


    /**
     * Delete a table.
     */
    public void deleteTable(String tableName) throws SQLException {
        if(!isTableCreated(tableName)) return;
        try(Statement statement = connection.createStatement()){
            String sql = "DROP TABLE "+tableName;
            statement.executeUpdate(sql);
        }
    }



}
