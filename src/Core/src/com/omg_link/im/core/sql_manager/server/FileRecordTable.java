package com.omg_link.im.core.sql_manager.server;

import com.omg_link.im.core.sql_manager.SqlManager;
import com.omg_link.im.core.sql_manager.Table;
import com.omg_link.sqlite_bridge.PreparedStatement;
import com.omg_link.sqlite_bridge.ResultSet;
import com.omg_link.sqlite_bridge.Statement;
import com.omg_link.utils.Sha512Digest;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FileRecordTable extends Table {

    private static final Column sha512Column = new Column("SHA512","BLOB",false,true);
    private static final Column fileIdColumn = new Column("FileId","CHAR(36)",false,false);

    private final PreparedStatement insertStatement;

    public FileRecordTable(SqlManager sqlManager) throws SQLException {
        super(sqlManager);
        insertStatement = sqlManager.prepareStatement(
                "INSERT INTO {tableName} VALUES(?,?)"
                        .replace("{tableName}",getTableName())
        );
    }

    public Map<Sha512Digest, UUID> getMapping() throws SQLException {
        Map<Sha512Digest, UUID> map = new HashMap<>();
        try(Statement statement = sqlManager.createStatement()){
            ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM {tableName}"
                            .replace("{tableName}",getTableName())
            );
            while(resultSet.next()){
                var sha512Digest = new Sha512Digest(resultSet.getBytes(sha512Column.name));
                var fileId = UUID.fromString(resultSet.getString(fileIdColumn.name));
                map.put(sha512Digest,fileId);
            }
        }
        return map;
    }

    public void setMapping(Sha512Digest digest,UUID fileId) throws SQLException {
        insertStatement.setBytes(1,digest.getData());
        insertStatement.setString(2,fileId.toString());
        insertStatement.executeUpdate();
    }

    @Override
    public String getTableName() {
        return "FileRecord";
    }

    @Override
    public Column[] getColumns() {
        return new Column[]{sha512Column,fileIdColumn};
    }

}
