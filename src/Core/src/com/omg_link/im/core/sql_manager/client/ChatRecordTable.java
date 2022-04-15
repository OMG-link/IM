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

    private static final Column isSelfSentColumn = new Column("IsSelfSent","TINYINT",false,false);

    private final PreparedStatement insertStatement;

    private long lastMessageId;

    public ChatRecordTable(SqlManager sqlManager) throws SQLException {
        super(sqlManager);
        insertStatement = sqlManager.prepareStatement(
                "INSERT INTO {tableName} VALUES (?,?,?)"
                        .replace("{tableName}", getTableName())
        );
    }

    public long getLastMessageId() {
        return lastMessageId;
    }

    public synchronized void addRecord(ChatRecord record) throws InvalidRecordException, SQLException {
        if (record.data.getLength() > Config.packageMaxLength) {
            throw new InvalidRecordException("Data too long!");
        }
        insertStatement.setLong(1, record.serialId);
        insertStatement.setLong(2, record.isSelfSent?1L:0L);
        insertStatement.setBytes(3, record.data.getBytes());
        insertStatement.executeInsert();
        lastMessageId = Math.max(lastMessageId, record.serialId);
    }

    public ChatRecord getRecord(long targetSerialId) throws InvalidSerialIdException, SQLException {
        try(Statement statement = sqlManager.createStatement()){
            try(Cursor cursor = statement.executeQuery(
                    "SELECT * FROM {tableName} WHERE {serialIdColumnName}={serialId}"
                            .replace("{tableName}", getTableName())
                            .replace("{serialIdColumnName}", serialIdColumn.name)
                            .replace("{serialId}",String.valueOf(targetSerialId))
            )){
                if (cursor.next()) {
                    long serialId = cursor.getLong(serialIdColumn.name);
                    boolean isSelfSent = cursor.getLong(isSelfSentColumn.name)==1;
                    ByteData data = new ByteData(cursor.getBytes(dataColumn.name));
                    return new ChatRecord(serialId,isSelfSent,data);
                } else {
                    throw new InvalidSerialIdException();
                }
            }
        }
    }

    @Override
    public String getTableName() {
        return "ChatRecord";
    }

    @Override
    public Column[] getColumns() {
        return new Column[]{serialIdColumn, isSelfSentColumn, dataColumn};
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
