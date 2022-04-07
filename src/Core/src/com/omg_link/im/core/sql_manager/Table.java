package com.omg_link.im.core.sql_manager;

import com.omg_link.im.core.sql_manager.components.ResultSet;
import com.omg_link.im.core.sql_manager.components.Statement;

import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.Objects;

public abstract class Table {

    public static class Column{
        public final String name;
        public final String dataType;
        public final boolean nullable;
        public final boolean isPrimaryKey;

        public Column(String name,String dataType,boolean nullable,boolean isPrimaryKey){
            this.name = name;
            this.dataType = dataType;
            this.nullable = nullable;
            this.isPrimaryKey = isPrimaryKey;
        }

    }

    public static String encodeString(String s){
        return '\''+s.replace("'","''")+'\'';
    }

    protected final SqlManager sqlManager;

    /**
     * <p>Constructor for the table.</p>
     * <p>It will load the table automatically.</p>
     * @param sqlManager SQL manager for this table.
     * @throws SQLException If an SQL error occurs when loading the table.
     */
    public Table(SqlManager sqlManager) throws SQLException {
        this.sqlManager = sqlManager;
        try{
            loadTable();
        }catch (InvalidTableException e){
            createTable();
        }
    }

    /**
     * @return The name of this table.
     */
    public abstract String getTableName();

    public abstract Column[] getColumns();

    public boolean isCreated() throws SQLException {
        return sqlManager.isTableCreated(getTableName());
    }

    /**
     * <p>Create a table.</p>
     * <p>If the table already exists, it will delete the old one.</p>
     * @throws SQLException If an SQL error occurs when creating the table.
     */
    public void createTable() throws SQLException {
        if(isCreated()){
            sqlManager.deleteTable(getTableName());
        }
        try(Statement statement = sqlManager.createStatement()){
            var sqlBuilder = new StringBuilder(
                    String.format("CREATE TABLE %s",getTableName())
            );
            boolean isFirstColumn = true;
            for(Column column:getColumns()){
                if(isFirstColumn){
                    sqlBuilder.append('(');
                    isFirstColumn = false;
                }else{
                    sqlBuilder.append(',');
                }
                sqlBuilder.append(
                        String.format("%s %s",column.name,column.dataType)
                );
                if(!column.nullable){
                    sqlBuilder.append(" NOT NULL");
                }
                if(column.isPrimaryKey){
                    sqlBuilder.append(" PRIMARY KEY");
                }
            }
            if(isFirstColumn){
                sqlBuilder.append("()");
            }else{
                sqlBuilder.append(')');
            }
            statement.executeUpdate(sqlBuilder.toString());
        }
    }

    public void insertRecord(String[] values) throws SQLException {
        if(values.length!= getColumns().length){
            throw new InvalidParameterException();
        }
        try(Statement statement = sqlManager.createStatement()){
            StringBuilder sqlBuilder = new StringBuilder(
                    String.format("INSERT INTO %s VALUES",getTableName())
            );
            boolean isFirstColumn = true;
            for(String value:values){
                if(isFirstColumn){
                    sqlBuilder.append('(');
                    isFirstColumn = false;
                }else{
                    sqlBuilder.append(',');
                }
                sqlBuilder.append(value);
            }
            if(isFirstColumn){
                sqlBuilder.append("()");
            }else{
                sqlBuilder.append(')');
            }
            statement.executeUpdate(sqlBuilder.toString());
        }
    }

    /**
     * <p>Load data from the table.</p>
     * @throws SQLException If an SQL error occurs when loading the table.
     * @throws InvalidTableException If the table does not match the required format.
     */
    public void loadTable() throws SQLException, InvalidTableException {
        try(Statement statement = sqlManager.createStatement()){
            ResultSet resultSet = statement.executeQuery(
                    "PRAGMA table_info({tableName})"
                            .replace("{tableName}",getTableName())
            );
            for(Column column:getColumns()){
                if(!resultSet.next()){
                    throw new InvalidTableException();
                }
                if(!Objects.equals(resultSet.getString("name"), column.name)){
                    throw new InvalidTableException();
                }
            }
        }
    }

}
