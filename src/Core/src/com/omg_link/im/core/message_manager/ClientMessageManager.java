package com.omg_link.im.core.message_manager;

import com.omg_link.im.core.ClientRoom;
import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data.PackageTooLargeException;
import com.omg_link.im.core.protocol.data_pack.DataPack;
import com.omg_link.im.core.protocol.data_pack.chat.*;
import com.omg_link.im.core.protocol.data_pack.file_transfer.FileTransferType;
import com.omg_link.im.core.sql_manager.InvalidRecordException;
import com.omg_link.im.core.sql_manager.InvalidSerialIdException;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ClientMessageManager {

    private final ClientRoom room;
    private final Map<UUID, IMessageSendCallback> uuidToCallbackMap = new HashMap<>();
    private final Map<Long, UUID> serialIdToUuidMap = new HashMap<>();

    private boolean enableSql;
    private long expectShowMin, expectShowMax, currentShowMin, currentShowMax;

    private final Map<Long, DataPack> chatRecord = new HashMap<>() {

        private DataPack getDataPackFromDB(Long serialId) {
            try {
                ByteData data = room.getSqlManager().getChatRecord(serialId);
                DataPack dataPack;
                try {
                    dataPack = byteDataToDataPack(data);
                } catch (InvalidPackageException e) {
                    throw new RuntimeException(e);
                }
                super.put(serialId, dataPack);
                return dataPack;
            } catch (SQLException e) {
                e.printStackTrace();
                disableSql();
                return null;
            } catch (InvalidSerialIdException e) {
                return null;
            }
        }

        private void putDataPackToDB(Long serialId, DataPack pack) {
            try {
                room.getSqlManager().addChatRecord(serialId, pack.encode());
            } catch (InvalidRecordException e) {
                throw new RuntimeException(e);
            } catch (SQLException e) {
                e.printStackTrace();
                disableSql();
            }
        }

        private final Map<Long, Boolean> isSerialIdInDatabase = new HashMap<>();

        @Override
        public boolean containsKey(Object key) {
            if (!(key instanceof Long)) return false;
            Long serialId = (Long) key;
            if (!isSerialIdInDatabase.containsKey(serialId)) {
                if (!enableSql) {
                    isSerialIdInDatabase.put(serialId, false);
                } else {
                    isSerialIdInDatabase.put(serialId, this.get(serialId) != null);
                }
            }
            return isSerialIdInDatabase.get(serialId);
        }

        @Override
        public DataPack get(Object key) {
            if (!(key instanceof Long)) return null;
            Long serialId = (Long) key;
            if (Objects.equals(isSerialIdInDatabase.get(serialId), true)) {
                return super.get(serialId);
            } else {
                if (!enableSql) {
                    return null;
                } else {
                    return getDataPackFromDB(serialId);
                }
            }
        }

        @Override
        public DataPack put(Long key, DataPack value) {
            var ret = super.put(key, value);
            if (!containsKey(key)) {
                putDataPackToDB(key, value);
                isSerialIdInDatabase.put(key, true);
            }
            return ret;
        }

    };

    public ClientMessageManager(ClientRoom room, long lastMessageSerialId, boolean enableSql) {
        this.room = room;
        this.enableSql = enableSql;

        this.currentShowMax = 0;
        this.currentShowMin = lastMessageSerialId+1;
        this.expectShowMin = lastMessageSerialId+1;
        this.expectShowMax = Long.MAX_VALUE;
        this.showMoreHistory();
    }

    public void disableSql() {
        enableSql = false;
    }

    private void addChatRecord(long serialId, DataPack pack) {
        if ((serialId > currentShowMax && serialId <= expectShowMax) || (serialId < currentShowMin && serialId >= expectShowMin)) {
            currentShowMax = serialId;
            showMessageOnUi(pack);
        }
        chatRecord.put(serialId, pack);
    }

    /**
     * Get a chat record according to the serial ID.
     *
     * @param serialId ID of record.
     * @return If the record exists either in memory or in database, return the record. Otherwise, return null.
     */
    private DataPack getChatRecord(long serialId) {
        return chatRecord.get(serialId);
    }

    /**
     * Ask the manager to give an ID to this callback.
     */
    private UUID getMsgIdForCallback(IMessageSendCallback callback) {
        while (true) {
            var uuid = UUID.randomUUID();
            if (!uuidToCallbackMap.containsKey(uuid)) {
                uuidToCallbackMap.put(uuid, callback);
                return uuid;
            }
        }
    }

    private void doCallback(long serialId, long stamp) {
        if (serialIdToUuidMap.containsKey(serialId)) {
            var uuid = serialIdToUuidMap.get(serialId);
            var callback = uuidToCallbackMap.get(uuid);
            if (callback != null) {
                callback.onSendSuccess(
                        serialId,
                        stamp
                );
            }
        }
    }

    private void send(DataPack pack) {
        room.getNetworkHandler().send(pack);
    }

    public void receive(ByteData data) throws InvalidPackageException {
        DataPack dataPack = byteDataToDataPack(data);
        switch (dataPack.getType()) {
            case ChatHistory: {
                var pack = (ChatHistoryPack) dataPack;
                var packs = pack.getPacks();
                for (var sPack : packs) {
                    receive(sPack);
                }
                break;
            }
            case ChatSendReply: {
                var pack = (ChatSendReplyPack) dataPack;
                if (pack.isOk()) {
                    assert (uuidToCallbackMap.containsKey(pack.getMsgId()));
                    serialIdToUuidMap.put(pack.getSerialId(), pack.getMsgId());
                }
                break;
            }
            case ChatText: {
                var pack = (ChatTextBroadcastPack) dataPack;
                doCallback(pack.getSerialId(), pack.getStamp());
                addChatRecord(pack.getSerialId(), pack);
                break;
            }
            case ChatImage: {
                var pack = (ChatImageBroadcastPack) dataPack;
                doCallback(pack.getSerialId(), pack.getStamp());
                addChatRecord(pack.getSerialId(), pack);
                break;
            }
            case ChatFile: {
                var pack = (ChatFileBroadcastPack) dataPack;
                doCallback(pack.getSerialId(), pack.getStamp());
                addChatRecord(pack.getSerialId(), pack);
                break;
            }
        }
    }

    private DataPack byteDataToDataPack(ByteData data) throws InvalidPackageException {
        var type = data.peekEnum(DataPack.Type.values());
        switch (type) {
            case ChatHistory: {
                return new ChatHistoryPack(data);
            }
            case ChatSendReply: {
                return new ChatSendReplyPack(data);
            }
            case ChatText: {
                return new ChatTextBroadcastPack(data);
            }
            case ChatImage: {
                return new ChatImageBroadcastPack(data);
            }
            case ChatFile: {
                return new ChatFileBroadcastPack(data);
            }
            default: {
                throw new InvalidPackageException();
            }
        }
    }

    private void showMessageOnUi(DataPack dataPack) {
        Long serialId = null;
        switch (dataPack.getType()) {
            case ChatText: {
                var pack = (ChatTextBroadcastPack) dataPack;
                serialId = pack.getSerialId();
                this.room.getRoomFrame().showTextMessage(
                        pack.getSerialId(),
                        pack.getUsername(),
                        pack.getStamp(),
                        pack.getText()
                );
                break;
            }
            case ChatImage: {
                var pack = (ChatImageBroadcastPack) dataPack;
                serialId = pack.getSerialId();
                UUID serverImageId = pack.getServerImageId();
                var panel = room.getRoomFrame().showChatImageMessage(
                        pack.getSerialId(),
                        pack.getUsername(),
                        pack.getStamp(),
                        serverImageId
                );
                if (room.getFileManager().isFileDownloaded(serverImageId)) {
                    try {
                        panel.onTransferSucceed(room.getFileManager().openFileByServerFileId(serverImageId));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e); //吊人搞我
                    }
                } else {
                    room.downloadFile(serverImageId.toString(), serverImageId, FileTransferType.ChatImage, panel);
                }
                break;
            }
            case ChatFile: {
                var pack = (ChatFileBroadcastPack) dataPack;
                serialId = pack.getSerialId();
                var panel = this.room.getRoomFrame().showFileUploadedMessage(
                        pack.getSerialId(),
                        pack.getUsername(),
                        pack.getStamp(),
                        pack.getFileId(),
                        pack.getFileName(),
                        pack.getFileSize()
                );
                break;
            }
        }
        if(serialId!=null){
            currentShowMax = Math.max(currentShowMax,serialId);
            currentShowMin = Math.min(currentShowMin,serialId);
        }
    }

    public void sendChatText(String text, IMessageSendCallback callback) throws PackageTooLargeException {
        var msgId = getMsgIdForCallback(callback);
        var pack = new ChatTextSendPack(msgId, text);
        send(pack);
    }

    // History

    public void getHistory() {
        getHistory(0);
    }

    public void getHistory(long lastSerialId) {
        send(new QueryHistoryPack(lastSerialId));
    }

    public void showMoreHistory() {
        boolean shouldSendGetHistoryPack = false;
        long tempExpectShowMin = expectShowMin;
        expectShowMin = Math.max(expectShowMin-Config.recordsPerPage,1);
        for (int i = Config.recordsPerPage; i >= 1 ; i--) {
            if(tempExpectShowMin-i<1) continue;
            DataPack dataPack = getChatRecord(tempExpectShowMin-i);
            if (dataPack != null) {
                showMessageOnUi(dataPack);
            } else {
                shouldSendGetHistoryPack = true;
            }
        }
        if(shouldSendGetHistoryPack){
            getHistory(tempExpectShowMin-1); // tempExpectMin是已经展示的最小值，因而查询的时候要减一
        }
    }

}
