package com.omg_link.im.core.sql_manager.server;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.sql_manager.InvalidRecordException;
import com.omg_link.im.core.sql_manager.InvalidSerialIdException;
import com.omg_link.im.core.sql_manager.SqlManager;
import com.omg_link.sqlite_bridge.SqlComponentFactory;
import com.omg_link.utils.Sha512Digest;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class ServerSqlManager extends SqlManager {

    private final BasicInfoTable basicInfoTable;
    private final ChatRecordTable chatRecordTable;
    private final FileRecordTable fileRecordTable;

    public ServerSqlManager(SqlComponentFactory factory, String fileName) throws SQLException {
        super(factory,fileName);
        basicInfoTable = new BasicInfoTable(this);
        chatRecordTable = new ChatRecordTable(this);
        fileRecordTable = new FileRecordTable(this);
    }

    public UUID getDatabaseUuid() {
        return basicInfoTable.getDatabaseUuid();
    }

    public long getChatRecordNum() {
        return chatRecordTable.getRecordNum();
    }

    public void addChatRecord(long serialId, ByteData data) throws InvalidSerialIdException, InvalidRecordException, SQLException {
        chatRecordTable.addRecord(serialId,data);
    }

    public ByteData getChatRecord(long serialId) throws InvalidSerialIdException,SQLException {
        return chatRecordTable.getRecord(serialId);
    }

    public Map<Sha512Digest,UUID> getFileDigestMapping() throws SQLException {
        return fileRecordTable.getMapping();
    }

    public void setFileDigestMapping(Sha512Digest digest,UUID fileId) throws SQLException {
        fileRecordTable.setMapping(digest,fileId);
    }

}
