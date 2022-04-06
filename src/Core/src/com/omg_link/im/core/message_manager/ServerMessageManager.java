package com.omg_link.im.core.message_manager;

import com.omg_link.im.core.ServerRoom;
import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.protocol.Attachment;
import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data_pack.DataPack;
import com.omg_link.im.core.protocol.data_pack.chat.*;
import com.omg_link.im.core.protocol.data_pack.file_transfer.FileTransferType;
import com.omg_link.im.core.sql_manager.InvalidRecordException;
import com.omg_link.im.core.sql_manager.InvalidSerialIdException;
import com.omg_link.im.core.sql_manager.server.ServerSqlManager;
import com.omg_link.im.core.user_manager.User;

import java.nio.channels.SelectionKey;
import java.sql.SQLException;
import java.util.UUID;

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

    private final ServerRoom serverRoom;
    private final SerialIdGenerator serialIdGenerator;

    private boolean enableSql;

    public ServerMessageManager(ServerRoom serverRoom, boolean enableSql) {
        this.serverRoom = serverRoom;
        this.enableSql = enableSql;

        var sqlManager = getSqlManager();
        if (sqlManager != null) {
            serialIdGenerator = new SerialIdGenerator(sqlManager.getChatRecordNum());
        } else {
            serialIdGenerator = new SerialIdGenerator(0);
        }
    }

    public void disableSql(){
        enableSql = false;
    }

    public long getLastSerialId(){
        return serialIdGenerator.getLastId();
    }

    private ServerSqlManager getSqlManager(){
        return serverRoom.getSqlManager();
    }

    private void addDataToHistory(long serialId, ByteData data) {
        if(!enableSql) return;
        try {
            getSqlManager().addChatRecord(serialId, data);
        } catch (InvalidRecordException|InvalidSerialIdException ignored) {
        } catch (SQLException e) {
            e.printStackTrace();
            disableSql();
        }
    }

    private ByteData getDataFromHistory(long serialId) throws InvalidSerialIdException {
        if(!enableSql) return null;
        if (serialId < 1 || serialId > serialIdGenerator.getLastId()) return null;
        try{
            return getSqlManager().getChatRecord(serialId);
        }catch (SQLException e){
            e.printStackTrace();
            disableSql();
            return null;
        }
    }

    /**
     * Send a data pack through the selection key.
     */
    private void send(SelectionKey selectionKey, DataPack dataPack) {
        serverRoom.getNetworkHandler().send(selectionKey, dataPack);
    }

    /**
     * <p>Broadcast a data pack.</p>
     * <p>The pack will also be recorded into the database.</p>
     */
    private void broadcast(long serialId, DataPack pack) {
        addDataToHistory(serialId, pack.encode());
        serverRoom.getNetworkHandler().broadcast(pack);
    }

    /**
     * Reject a send request.
     */
    private void rejectSendRequest(SelectionKey selectionKey, ChatSendReplyPack.Reason state) {
        send(selectionKey, new ChatSendReplyPack(state));
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
        for (int i = Config.recordsPerPage - 1; i >= 0; i--) {
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
    protected void broadcastChatImage(User user, UUID imageId) {
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
    protected void broadcastChatFile(User user, UUID fileId, String fileName, long fileSize) {
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

    public void onFileUploaded(User user, UUID fileId, String fileName, long fileSize, FileTransferType fileTransferType){
        switch (fileTransferType){
            case ChatFile:{
                serverRoom.getMessageManager().broadcastChatFile(
                        user,
                        fileId,
                        fileName,
                        fileSize
                );
                break;
            }
            case ChatImage:{
                serverRoom.getMessageManager().broadcastChatImage(
                        user,
                        fileId
                );
                break;
            }
        }
    }

}
