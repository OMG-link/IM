package com.omg_link.im.core.sql_manager.client;

import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.sql_manager.InvalidTableException;
import com.omg_link.im.core.sql_manager.SqlManager;
import com.omg_link.im.core.sql_manager.Table;
import com.omg_link.im.core.sql_manager.components.ResultSet;
import com.omg_link.im.core.sql_manager.components.Statement;

import java.sql.SQLException;
import java.util.UUID;

class BasicInfoTable extends Table {

    private static final Column serverIdColumn = new Column("ServerId","CHAR(36)",false,false);
    private static final Column dbVersionColumn = new Column("DBVersion","TEXT",false,false);

    private UUID serverId;

    public BasicInfoTable(SqlManager sqlManager,UUID serverId) throws SQLException {
        super(sqlManager);
        this.setServerId(serverId);
    }

    public UUID getServerId() {
        return serverId;
    }

    private void setServerId(UUID serverId) throws SQLException {
        this.serverId = serverId;
        try(Statement statement = sqlManager.createStatement()){
            statement.executeUpdate(
                    "UPDATE {tableName} set {serverIdColumn}='{serverId}'"
                            .replace("{tableName}",getTableName())
                            .replace("{serverIdColumn}",serverIdColumn.name)
                            .replace("{serverId}",serverId.toString())
            );
        }
    }

    @Override
    public String getTableName() {
        return "BasicInfo";
    }

    @Override
    public Column[] getColumns() {
        return new Column[]{
                serverIdColumn, dbVersionColumn
        };
    }

    @Override
    public void createTable() throws SQLException {
        super.createTable();
        serverId = new UUID(0,0);
        insertRecord(new String[]{
                encodeString(serverId.toString()),
                encodeString(Config.compatibleVersion)
        });
    }

    @Override
    public void loadTable() throws SQLException, InvalidTableException {
        super.loadTable();
        try(Statement statement = sqlManager.createStatement()){
            String sql = "SELECT * FROM " + getTableName();
            ResultSet resultSet = statement.executeQuery(sql);
            if(resultSet.next()){
                String sServerId,sDBVersion;
                try{
                    sServerId = resultSet.getString(serverIdColumn.name);
                    sDBVersion = resultSet.getString(dbVersionColumn.name);
                }catch (SQLException e){
                    throw new InvalidTableException();
                }
                serverId = UUID.fromString(sServerId);
                if(!Config.compatibleVersion.equals(sDBVersion)){
                    throw new SQLException(String.format(
                            "The server is running at version %s, while the database is written in version %s.\nFor safety reasons, the database will not be used.",
                            dbVersionColumn.name,
                            sDBVersion
                    ));
                }
            }else{
                throw new InvalidTableException(); //There is something wrong with the table
            }
        }
    }

}
