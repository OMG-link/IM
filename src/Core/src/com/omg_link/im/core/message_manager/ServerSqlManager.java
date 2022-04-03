package com.omg_link.im.core.message_manager;

import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.protocol.data.ByteData;

import java.sql.*;
import java.util.UUID;

public class ServerSqlManager {

    private static final String basicInfoTableName = "BasicInfo";
    private static final String tableUuidColumnName = "TableUuid";
    private static final String serverVersionColumnName = "ServerVersion";

    private static final String recordTableName = "ChatRecord";
    private static final String serialIdColumnName = "SerialId";
    private static final String dataColumnName = "Data";

    private final Connection connection;
    private final PreparedStatement insertStatement;
    private final PreparedStatement queryStatement;

    private long recordNum;
    private UUID tableUuid;

    public ServerSqlManager(String fileName) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
        // Make connection
        connection = DriverManager.getConnection("jdbc:sqlite:" + fileName);
        // Load basic info table
        try{
            loadBasicInfoTable();
        }catch (InvalidTableException e){
            createBasicInfoTable();
        }
        // Load record table
        try{
            loadRecordTable();
        }catch (InvalidTableException e){
            createRecordTable();
        }
        insertStatement = connection.prepareStatement(
                "INSERT INTO "+ recordTableName +" ("+serialIdColumnName+","+dataColumnName+") VALUES (?,?)"
        );
        queryStatement = connection.prepareStatement(
                "SELECT "+serialIdColumnName+","+dataColumnName+" FROM "+ recordTableName +" WHERE serialId=?"
        );
    }

    public void put(long serialId, ByteData data) throws InvalidRecordException {
        if (data.getLength() > Config.packageMaxLength) {
            throw new InvalidRecordException("Data too long!");
        }
        try {
            insertStatement.setLong(1, serialId);
            insertStatement.setBytes(2, data.getBytes());
            insertStatement.executeUpdate();
            recordNum++;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ByteData get(long serialId) throws IndexOutOfBoundsException {
        if(serialId<=0||serialId>recordNum){
            throw new IndexOutOfBoundsException(((Long)serialId).toString());
        }
        try{
            queryStatement.setLong(1,serialId);
            ResultSet resultSet = queryStatement.executeQuery();
            if(resultSet.next()){
                return new ByteData(resultSet.getBytes(dataColumnName));
            }else{
                throw new RuntimeException(String.format("Cannot find record with serialId=%d.",serialId));
            }
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public UUID getTableUuid() {
        return tableUuid;
    }

    public long getRecordNum() {
        return recordNum;
    }

    /**
     * Check whether the table has been created.
     */
    private boolean isTableCreated(String tableName) throws SQLException {
        try(Statement statement = connection.createStatement()){
            String sql = "PRAGMA table_info("+tableName+")";
            ResultSet resultSet = statement.executeQuery(sql);
            return resultSet.next();
        }
    }

    /**
     * Delete a table.
     */
    private void deleteTable(String tableName) throws SQLException {
        if(!isTableCreated(tableName)) return;
        try(Statement statement = connection.createStatement()){
            String sql = "DROP TABLE "+tableName;
            statement.executeUpdate(sql);
        }
    }

    /**
     * <p>Create basic info table.</p>
     * <p>It will set table UUID to a new value.</p>
     */
    private void createBasicInfoTable() throws SQLException {
        if(isTableCreated(basicInfoTableName)){
            deleteTable(basicInfoTableName);
        }
        tableUuid = UUID.randomUUID();
        try(Statement statement = connection.createStatement()){
            String sql;
            sql = "CREATE TABLE " + basicInfoTableName + " ( " +
                    tableUuidColumnName     + " CHAR(36) NOT NULL," +
                    serverVersionColumnName + " TEXT     NOT NULL)";
            statement.executeUpdate(sql);
            sql = "INSERT INTO " + basicInfoTableName + " VALUES (" +
                    "'" + tableUuid.toString() + "'," +
                    "'" + Config.compatibleVersion + "')";
            statement.executeUpdate(sql);
        }
    }

    /**
     * Create record table.
     */
    private void createRecordTable() throws SQLException {
        if(isTableCreated(recordTableName)){
            deleteTable(recordTableName);
        }
        recordNum = 0;
        try(Statement statement = connection.createStatement()){
            String sql = "CREATE TABLE " + recordTableName + " ( " +
                    serialIdColumnName + " INT8 PRIMARY KEY NOT NULL," +
                    dataColumnName     + " BLOB             NOT NULL)";
            statement.executeUpdate(sql);
        }
    }

    /**
     * Load the basic info table to get table UUID.
     * @throws InvalidTableException If the basic info table does not exist or the format is wrong.
     */
    private void loadBasicInfoTable() throws SQLException, InvalidTableException {
        if(!isTableCreated(basicInfoTableName)){
            throw new InvalidTableException();
        }
        try(Statement statement = connection.createStatement()){
            String sql = "SELECT * FROM " + basicInfoTableName;
            ResultSet resultSet = statement.executeQuery(sql);
            if(resultSet.next()){
                String sTableUuid,sServerVersion;
                try{
                    sTableUuid = resultSet.getString(tableUuidColumnName);
                    sServerVersion = resultSet.getString(serverVersionColumnName);
                }catch (SQLException e){
                    throw new InvalidTableException();
                }
                tableUuid = UUID.fromString(sTableUuid);
                if(!Config.compatibleVersion.equals(sServerVersion)){
                    throw new SQLException(String.format(
                            "The server is running at version %s, while the database is written in version %s.\nFor safety reasons, the database will not be used.",
                            serverVersionColumnName,
                            sServerVersion
                    ));
                }
            }else{
                throw new InvalidTableException(); //There is something wrong with the table
            }
        }
    }

    /**
     * <p>Load record table to get the record count.</p>
     * @throws InvalidTableException When the format of columns is wrong.
     */
    private void loadRecordTable() throws SQLException, InvalidTableException {
        if(!isTableCreated(recordTableName)){
            throw new InvalidTableException();
        }
        try(Statement statement = connection.createStatement()){
            String sql = "SELECT COUNT(*) as rowCount FROM " + recordTableName;
            ResultSet resultSet = statement.executeQuery(sql);
            if(resultSet.next()){
                recordNum = resultSet.getLong("rowCount");
            }else{
                throw new SQLException("WTF?");
            }
        }
    }

}
