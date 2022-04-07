package com.omg_link.im.core.sql_manager.client;

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

    private long lastMessageId;

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

    public long getLastMessageId() {
        return lastMessageId;
    }

    public synchronized void addRecord(long serialId, ByteData data) throws InvalidRecordException, SQLException {
        if (data.getLength() > Config.packageMaxLength) {
            throw new InvalidRecordException("Data too long!");
        }
        insertStatement.setLong(1, serialId);
        insertStatement.setBytes(2, data.getBytes());
        insertStatement.executeUpdate();
        lastMessageId = Math.max(lastMessageId,serialId);
    }

    public ByteData getRecord(long serialId) throws InvalidSerialIdException, SQLException {
        queryStatement.setLong(1, serialId);
        try(Cursor cursor = queryStatement.executeQuery()){
            if (cursor.next()) {
                return new ByteData(cursor.getBytes(dataColumn.name));
            } else {
                throw new InvalidSerialIdException();
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
        lastMessageId = 0;
    }

    @Override
    public void loadTable() throws SQLException, InvalidTableException {
        super.loadTable();
        try (Statement statement = sqlManager.createStatement()) {
            try(Cursor cursor = statement.executeQuery(
                    "SELECT MAX({SerialIdColumn}) as lastMessageId FROM {tableName}"
                            .replace("{SerialIdColumn}", serialIdColumn.name)
                            .replace("{tableName}", getTableName())
            )){
                if (cursor.next()) {
                    lastMessageId = cursor.getLong("lastMessageId");
                } else {
                    throw new SQLException("WTF?");
                }
            }
        }
    }
}
