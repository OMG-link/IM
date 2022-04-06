package com.omg_link.im.core.sql_manager.client;

import com.omg_link.im.core.sql_manager.SqlManager;
import com.omg_link.im.core.sql_manager.Table;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FileRecordTable extends Table {

    private static final Column fileIdColumn = new Column("FileId", "CHAR(36)", false, true);
    private static final Column filePathColumn = new Column("FilePath", "TEXT", false, false);

    private final PreparedStatement insertStatement;
    private final PreparedStatement removeStatement;

    public FileRecordTable(SqlManager sqlManager) throws SQLException {
        super(sqlManager);
        insertStatement = sqlManager.prepareStatement(
                "INSERT INTO {tableName} VALUES(?,?)"
                        .replace("{tableName}", getTableName())
        );
        removeStatement = sqlManager.prepareStatement(
                "DELETE FROM {tableName} WHERE {fileIdColumn}=?"
                        .replace("{tableName}", getTableName())
                        .replace("{fileIdColumn}", fileIdColumn.name)
        );
    }

    public Map<UUID, File> getMapping() throws SQLException {
        Map<UUID, File> map = new HashMap<>();
        try (Statement statement = sqlManager.createStatement()) {
            ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM {tableName}"
                            .replace("{tableName}", getTableName())
            );
            while (resultSet.next()) {
                var fileId = UUID.fromString(resultSet.getString(fileIdColumn.name));
                var file = new File(resultSet.getString(filePathColumn.name));
                map.put(fileId, file);
            }
        }
        return map;
    }

    public void addMapping(UUID fileId, File file) throws SQLException {
        insertStatement.setString(1, fileId.toString());
        insertStatement.setString(2, file.getAbsolutePath());
        insertStatement.executeUpdate();
    }

    public void removeMapping(UUID fileId) throws SQLException {
        removeStatement.setString(1,fileId.toString());
        removeStatement.executeUpdate();
    }

    @Override
    public String getTableName() {
        return "FileRecord";
    }

    @Override
    public Column[] getColumns() {
        return new Column[]{fileIdColumn, filePathColumn};
    }

}
