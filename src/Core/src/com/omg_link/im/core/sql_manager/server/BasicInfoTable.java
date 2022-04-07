package com.omg_link.im.core.sql_manager.server;

import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.sql_manager.InvalidTableException;
import com.omg_link.im.core.sql_manager.SqlManager;
import com.omg_link.im.core.sql_manager.Table;
import com.omg_link.im.core.sql_manager.components.ResultSet;
import com.omg_link.im.core.sql_manager.components.Statement;

import java.sql.SQLException;
import java.util.UUID;

class BasicInfoTable extends Table {

    private static final Column databaseUuidColumn = new Column("DatabaseUuid","CHAR(36)",false,false);
    private static final Column serverVersionColumn = new Column("ServerVersion","TEXT",false,false);

    private UUID databaseUuid;

    public BasicInfoTable(SqlManager sqlManager) throws SQLException {
        super(sqlManager);
    }

    public UUID getDatabaseUuid() {
        return databaseUuid;
    }

    @Override
    public String getTableName() {
        return "BasicInfo";
    }

    @Override
    public Column[] getColumns() {
        return new Column[]{
                databaseUuidColumn, serverVersionColumn
        };
    }

    @Override
    public void createTable() throws SQLException {
        super.createTable();
        databaseUuid = UUID.randomUUID();
        insertRecord(new String[]{
                encodeString(databaseUuid.toString()),
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
                String sDataBaseUuid,sServerVersion;
                try{
                    sDataBaseUuid = resultSet.getString(databaseUuidColumn.name);
                    sServerVersion = resultSet.getString(serverVersionColumn.name);
                }catch (SQLException e){
                    throw new InvalidTableException();
                }
                databaseUuid = UUID.fromString(sDataBaseUuid);
                if(!Config.compatibleVersion.equals(sServerVersion)){
                    throw new SQLException(String.format(
                            "The server is running at version %s, while the database is written in version %s.\nFor safety reasons, the database will not be used.",
                            serverVersionColumn.name,
                            sServerVersion
                    ));
                }
            }else{
                throw new InvalidTableException(); //There is something wrong with the table
            }
        }
    }

}
