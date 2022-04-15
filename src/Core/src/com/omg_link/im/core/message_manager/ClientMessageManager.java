package com.omg_link.im.core.message_manager;

import com.omg_link.im.core.ClientRoom;
import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data.PackageTooLargeException;
import com.omg_link.im.core.protocol.data_pack.DataPack;
import com.omg_link.im.core.protocol.data_pack.chat.*;
import com.omg_link.im.core.protocol.file_transfer.FileTransferType;
import com.omg_link.im.core.sql_manager.InvalidRecordException;
import com.omg_link.im.core.sql_manager.InvalidSerialIdException;
import com.omg_link.im.core.sql_manager.client.ChatRecord;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.*;

public class ClientMessageManager {

    private final ClientRoom room;

    private boolean enableSql;
    private long expectShowMin, expectShowMax, currentShowMin, currentShowMax;

    private final Set<Long> selfSentMessageSet = new HashSet<>();

    private final Map<Long, ChatRecord> chatRecordMap = new HashMap<>() {

        private ChatRecord getDataPackFromDB(Long serialId) {
            try {
                ChatRecord record = room.getSqlManager().getChatRecord(serialId);
                super.put(serialId, record);
                return record;
            } catch (SQLException e) {
                e.printStackTrace();
                disableSql();
                return null;
            } catch (InvalidSerialIdException e) {
                return null;
            }
        }

        private void putDataPackToDB(ChatRecord record) {
            try {
                room.getSqlManager().addChatRecord(record);
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
        public ChatRecord get(Object key) {
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
        public ChatRecord put(Long key, ChatRecord value) {
            var ret = super.put(key, value);
            if (!containsKey(key)) {
                putDataPackToDB(value);
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

    private void addChatRecord(long serialId, boolean isSelfSent, DataPack pack) {
        if ((serialId > currentShowMax && serialId <= expectShowMax) || (serialId < currentShowMin && serialId >= expectShowMin)) {
            currentShowMax = serialId;
            showMessageOnUi(pack, isSelfSent);
        }
        chatRecordMap.put(serialId, new ChatRecord(serialId,isSelfSent,pack.encode()));
    }

    /**
     * Get a chat record according to the serial ID.
     *
     * @param serialId ID of record.
     * @return If the record exists either in memory or in database, return the record. Otherwise, return null.
     */
    private ChatRecord getChatRecord(long serialId) {
        return chatRecordMap.get(serialId);
    }

    private void send(DataPack pack) {
        room.getNetworkHandler().send(pack);
    }

    public void receive(ByteData data) throws InvalidPackageException {
        DataPack dataPack = byteDataToDataPack(data);
        switch (dataPack.getType()) {
            case SelfSentNotice:{
                var pack = (SelfSentNotice) dataPack;
                selfSentMessageSet.add(pack.getSerialId());
                break;
            }
            case ChatHistory: {
                var pack = (ChatHistoryPack) dataPack;
                var packs = pack.getPacks();
                for (var sPack : packs) {
                    receive(sPack);
                }
                break;
            }
            case ChatText: {
                var pack = (ChatTextBroadcastPack) dataPack;
                addChatRecord(
                        pack.getSerialId(),
                        selfSentMessageSet.contains(pack.getSerialId()),
                        pack
                );
                break;
            }
            case ChatImage: {
                var pack = (ChatImageBroadcastPack) dataPack;
                addChatRecord(
                        pack.getSerialId(),
                        selfSentMessageSet.contains(pack.getSerialId()),
                        pack
                );
                break;
            }
            case ChatFile: {
                var pack = (ChatFileBroadcastPack) dataPack;
                addChatRecord(
                        pack.getSerialId(),
                        selfSentMessageSet.contains(pack.getSerialId()),
                        pack
                );
                break;
            }
        }
    }

    private DataPack byteDataToDataPack(ByteData data) throws InvalidPackageException {
        var type = data.peekEnum(DataPack.Type.values());
        switch (type) {
            case SelfSentNotice:{
                return new SelfSentNotice(data);
            }
            case ChatHistory: {
                return new ChatHistoryPack(data);
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

    private void showMessageOnUi(DataPack dataPack, boolean isSelfSent) {
        Long serialId = null;
        switch (dataPack.getType()) {
            case ChatText: {
                var pack = (ChatTextBroadcastPack) dataPack;
                serialId = pack.getSerialId();
                this.room.getRoomFrame().showTextMessage(
                        pack,
                        isSelfSent
                );
                break;
            }
            case ChatImage: {
                var pack = (ChatImageBroadcastPack) dataPack;
                serialId = pack.getSerialId();
                UUID serverImageId = pack.getServerImageId();
                var panel = room.getRoomFrame().showChatImageMessage(
                        pack,
                        isSelfSent
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
                        pack,
                        isSelfSent
                );
                break;
            }
        }
        if(serialId!=null){
            currentShowMax = Math.max(currentShowMax,serialId);
            currentShowMin = Math.min(currentShowMin,serialId);
        }
    }

    public void sendChatText(String text) throws PackageTooLargeException {
        var pack = new ChatTextSendPack(text);
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
            ChatRecord record = getChatRecord(tempExpectShowMin-i);
            DataPack pack = null;
            if(record!=null){
                try{
                    pack = byteDataToDataPack(record.data);
                }catch (InvalidPackageException e){
                    record = null;
                }
            }
            if (record != null) {
                showMessageOnUi(pack,record.isSelfSent);
            } else {
                shouldSendGetHistoryPack = true;
            }
        }
        if(shouldSendGetHistoryPack){
            getHistory(tempExpectShowMin-1); // tempExpectMin是已经展示的最小值，因而查询的时候要减一
        }
    }

}
