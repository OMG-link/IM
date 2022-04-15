package com.omg_link.im.core.sql_manager.client;

import com.omg_link.im.core.sql_manager.InvalidRecordException;
import com.omg_link.im.core.sql_manager.InvalidSerialIdException;
import com.omg_link.im.core.sql_manager.SqlManager;
import com.omg_link.sqlite_bridge.SqlComponentFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class ClientSqlManager extends SqlManager {

    private final BasicInfoTable basicInfoTable;
    private final ChatRecordTable chatRecordTable;
    private final FileRecordTable fileRecordTable;

    public ClientSqlManager(SqlComponentFactory factory, String fileName, UUID serverId) throws SQLException {
        super(factory,fileName);
        this.basicInfoTable = new BasicInfoTable(this,serverId);
        this.chatRecordTable = new ChatRecordTable(this);
        this.fileRecordTable = new FileRecordTable(this);
    }

    public void addChatRecord(ChatRecord record) throws InvalidRecordException, SQLException {
        chatRecordTable.addRecord(record);
    }

    public ChatRecord getChatRecord(long serialId) throws InvalidSerialIdException, SQLException {
        return chatRecordTable.getRecord(serialId);
    }

    public long getLastSerialId() {
        return chatRecordTable.getLastMessageId();
    }

    public Map<UUID, File> getFileMapping() throws SQLException {
        return fileRecordTable.getMapping();
    }

    public void addFileMapping(UUID fileId,File file) throws SQLException {
        fileRecordTable.addMapping(fileId,file);
    }

    public void removeFileMapping(UUID fileId) throws SQLException {
        fileRecordTable.removeMapping(fileId);
    }

}
