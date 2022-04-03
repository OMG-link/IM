package com.omg_link.im.core.message_manager;

import com.omg_link.im.core.Server;
import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.protocol.Attachment;
import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data_pack.DataPack;
import com.omg_link.im.core.protocol.data_pack.chat.*;
import com.omg_link.im.core.user_manager.User;

import java.nio.channels.SelectionKey;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class ServerMessageManager {

    private static class SerialIdGenerator {
        private long serialId;

        public SerialIdGenerator(long begin) {
            serialId = begin;
        }

        public long getLastId() {
            return serialId;
        }

        public synchronized long getNewId() {
            return ++serialId;
        }

    }

    private final Server server;

    private ServerSqlManager sqlManager;

    private final SerialIdGenerator serialIdGenerator;

    public ServerMessageManager(Server server) {
        this.server = server;
        try {
            sqlManager = new ServerSqlManager(Config.getServerDatabasePath());
        } catch (SQLException e) {
            e.printStackTrace();
            server.getLogger().log(
                    Level.WARNING,
                    "Cannot open database file. Chat history will not be logged."
            );
        }
        if (sqlManager != null) {
            serialIdGenerator = new SerialIdGenerator(sqlManager.getRecordNum());
        } else {
            serialIdGenerator = new SerialIdGenerator(0);
        }
    }

    public UUID getDialogId() {
        if (sqlManager == null) {
            return UUID.randomUUID();
        } else {
            return sqlManager.getTableUuid();
        }
    }

    private void addDataToHistory(long serialId, ByteData data) {
        if (sqlManager == null) return;
        try {
            sqlManager.put(serialId, data);
        } catch (InvalidRecordException ignored) {
        }
    }

    private ByteData getDataFromHistory(long serialId) throws InvalidSerialIdException {
        if (serialId < 1 || serialId > serialIdGenerator.getLastId()) return null;
        if (sqlManager == null) return null;
        return sqlManager.get(serialId);
    }

    /**
     * Send a data pack through the selection key.
     */
    private void send(SelectionKey selectionKey, DataPack dataPack) {
        server.getNetworkHandler().send(selectionKey, dataPack);
    }

    /**
     * <p>Broadcast a data pack.</p>
     * <p>The pack will also be recorded into the database.</p>
     */
    private void broadcast(long serialId, DataPack pack) {
        addDataToHistory(serialId, pack.encode());
        server.getNetworkHandler().broadcast(pack);
    }

    /**
     * Reject a send request.
     */
    private void rejectSendRequest(SelectionKey selectionKey, ChatSendReplyPack.Reason reason) {
        send(selectionKey, new ChatSendReplyPack(reason));
    }

    /**
     * Accept a send request.
     *
     * @return The serial ID for this request.
     */
    private long acceptSendRequest(SelectionKey selectionKey, UUID msgId) {
        long serialId = serialIdGenerator.getNewId();
        send(selectionKey, new ChatSendReplyPack(
                msgId,
                serialId
        ));
        return serialId;
    }

    /**
     * <p>Send history data to user.</p>
     * <p>It will not send ChatHistoryPack when no history data should be send.</p>
     *
     * @param selectionKey The selection key of the user.
     * @param lastSerialId The serialId of the latest record.
     * @throws InvalidSerialIdException When {@code lastSerialId} is invalid.
     */
    public void sendHistory(SelectionKey selectionKey, long lastSerialId) throws InvalidSerialIdException {
        if (lastSerialId < 0 || lastSerialId > serialIdGenerator.getLastId()) {
            throw new InvalidSerialIdException();
        }
        if (lastSerialId == 0) {
            lastSerialId = serialIdGenerator.getLastId();
        }
        var pack = new ChatHistoryPack();
        for (int i = 15 - 1; i >= 0; i--) {
            ByteData data = getDataFromHistory(lastSerialId - i);
            if (data != null) {
                pack.addPack(data);
            }
        }
        if (pack.getPacks().size() == 0) return;
        send(selectionKey, pack);
    }

    /**
     * Process a ChatTextPack.
     */
    public void processChatTextPack(SelectionKey selectionKey, ChatTextSendPack pack) {
        if (pack.getText().length() > Config.chatTextMaxLength) {
            rejectSendRequest(selectionKey, ChatSendReplyPack.Reason.ChatTextTooLong);
            return;
        }
        Attachment attachment = (Attachment) selectionKey.attachment();
        long serialId = acceptSendRequest(selectionKey, pack.getMsgId());
        broadcastChatText(serialId, attachment.user, pack.getText());
    }

    /**
     * Broadcast a ChatTextPack
     */
    public void broadcastChatText(long serialId, User user, String text) {
        var pack = new ChatTextBroadcastPack(
                serialId,
                user,
                text
        );
        broadcast(serialId, pack);
    }

    /**
     * Broadcast a ChatImagePack
     */
    public void broadcastChatImage(User user, UUID imageId) {
        var serialId = serialIdGenerator.getNewId();
        var pack = new ChatImageBroadcastPack(
                serialId,
                user,
                imageId
        );
        broadcast(serialId, pack);
    }

    /**
     * Broadcast a ChatFilePack
     */
    public void broadcastChatFile(User user, UUID fileId, String fileName, long fileSize) {
        var serialId = serialIdGenerator.getNewId();
        var pack = new ChatFileBroadcastPack(
                serialId,
                user,
                fileId,
                fileName,
                fileSize
        );
        broadcast(serialId, pack);
    }

}
