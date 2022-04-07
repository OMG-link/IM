package com.omg_link.im.core.sql_manager;

import com.omg_link.im.core.sql_manager.components.*;
import com.omg_link.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public abstract class SqlManager {
    protected final Connection connection;

    public SqlManager(SqlComponentFactory factory, String fileName) throws SQLException {
        // Make connection
        try{
            FileUtils.makeFolder(new File(fileName).getAbsoluteFile().getParentFile());
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        connection = factory.createConnection(fileName);
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
        try(Statement statement = createStatement()){
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
        try(Statement statement = createStatement()){
            String sql = "DROP TABLE "+tableName;
            statement.executeUpdate(sql);
        }
    }



}
