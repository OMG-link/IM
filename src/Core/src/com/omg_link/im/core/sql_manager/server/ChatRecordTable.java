package com.omg_link.im.core.sql_manager.server;

import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.sql_manager.*;
import com.omg_link.sqlite_bridge.PreparedStatement;
import com.omg_link.sqlite_bridge.Cursor;
import com.omg_link.sqlite_bridge.Statement;

import java.sql.SQLException;

public class ChatRecordTable extends Table {

    private static final Column serialIdColumn = new Column("SerialId", "INT8", false, true);
    private static final Column dataColumn = new Column("Data", "BLOB", false, false);

    private final PreparedStatement insertStatement;
    private final PreparedStatement queryStatement;

    private long recordNum;

    public ChatRecordTable(SqlManager sqlManager) throws SQLException {
        super(sqlManager);
        insertStatement = sqlManager.prepareStatement(
                "INSERT INTO {tableName} VALUES (?,?)"
                        .replace("{tableName}", getTableName())
        );
        queryStatement = sqlManager.prepareStatement(
                "SELECT * FROM {tableName} WHERE {serialIdColumnName}=?"
                        .replace("{tableName}", getTableName())
                        .replace("{serialIdColumnName}", serialIdColumn.name)
        );
    }

    public long getRecordNum() {
        return recordNum;
    }

    public synchronized void addRecord(long serialId, ByteData data) throws InvalidRecordException, InvalidSerialIdException, SQLException {
        if (data.getLength() > Config.packageMaxLength) {
            throw new InvalidRecordException("Data too long!");
        }
        if (serialId != recordNum + 1) {
            throw new InvalidSerialIdException();
        }
        insertStatement.setLong(1, serialId);
        insertStatement.setBytes(2, data.getBytes());
        insertStatement.executeInsert();
        recordNum++;
    }

    public ByteData getRecord(long serialId) throws InvalidSerialIdException, SQLException {
        if (serialId < 1 || serialId > recordNum) {
            throw new InvalidSerialIdException();
        }
        queryStatement.setLong(1, serialId);
        try(Cursor cursor = queryStatement.executeQuery()){
            if (cursor.next()) {
                return new ByteData(cursor.getBytes(dataColumn.name));
            } else {
                throw new RuntimeException(String.format("Cannot find record with serialId=%d.", serialId));
            }
        }
    }

    @Override
    public String getTableName() {
        return "ChatRecord";
    }

    @Override
    public Column[] getColumns() {
        return new Column[]{serialIdColumn, dataColumn};
    }

    @Override
    public void createTable() throws SQLException {
        super.createTable();
        recordNum = 0;
    }

    @Override
    public void loadTable() throws SQLException, InvalidTableException {
        super.loadTable();
        try (Statement statement = sqlManager.createStatement()) {
            try(Cursor cursor = statement.executeQuery(
                    "SELECT COUNT(*) as rowCount FROM {tableName}"
                            .replace("{tableName}", getTableName())
            )){
                if (cursor.next()) {
                    recordNum = cursor.getLong("rowCount");
                } else {
                    throw new SQLException("WTF?");
                }
            }
        }
    }
}
