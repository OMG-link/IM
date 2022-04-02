package com.omg_link.im.message_manager;

import com.omg_link.im.Client;
import com.omg_link.im.protocol.data.ByteData;
import com.omg_link.im.protocol.data.InvalidPackageException;
import com.omg_link.im.protocol.data.PackageTooLargeException;
import com.omg_link.im.protocol.data_pack.DataPack;
import com.omg_link.im.protocol.data_pack.chat.*;
import com.omg_link.im.protocol.data_pack.file_transfer.FileTransferType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientMessageManager {

    private final Client client;
    private final Map<UUID, IMessageSendCallback> uuidToCallbackMap = new HashMap<>();
    private final Map<Long,UUID> serialIdToUuidMap = new HashMap<>();

    public ClientMessageManager(Client client){
        this.client = client;
    }

    private UUID getMsgId(IMessageSendCallback callback){
        while(true){
            var uuid = UUID.randomUUID();
            if(!uuidToCallbackMap.containsKey(uuid)){
                uuidToCallbackMap.put(uuid,callback);
                return uuid;
            }
        }
    }

    private void doCallback(long serialId, long stamp){
        if(serialIdToUuidMap.containsKey(serialId)){
            var uuid = serialIdToUuidMap.get(serialId);
            var callback = uuidToCallbackMap.get(uuid);
            if(callback!=null){
                callback.onSendSuccess(
                        serialId,
                        stamp
                );
            }
        }
    }

    private void send(DataPack pack) throws PackageTooLargeException {
        client.getNetworkHandler().send(pack);
    }

    public void receive(ByteData data) throws InvalidPackageException {
        var type = data.peekEnum(DataPack.Type.values());
        switch (type){
            case ChatHistory:{
                var pack = new ChatHistoryPack(data);
                var packs = pack.getPacks();
                for(var sPack:packs){
                    receive(sPack);
                }
                break;
            }
            case ChatSendReply:{
                var pack = new ChatSendReplyPack(data);
                if(pack.isOk()){
                    assert(uuidToCallbackMap.containsKey(pack.getMsgId()));
                    serialIdToUuidMap.put(pack.getSerialId(), pack.getMsgId());
                }
                break;
            }
            case ChatText:{
                var pack = new ChatTextBroadcastPack(data);
                doCallback(pack.getSerialId(),pack.getStamp());
                this.client.getRoomFrame().showTextMessage(
                        pack.getSerialId(),
                        pack.getUsername(),
                        pack.getStamp(),
                        pack.getText()
                );
                break;
            }
            case ChatImage:{
                var pack = new ChatImageBroadcastPack(data);
                var callback = client.getRoomFrame().showChatImageMessage(
                        pack.getSerialId(),
                        pack.getUsername(),
                        pack.getStamp(),
                        pack.getServerImageId()
                );
                client.downloadFile(pack.getServerImageId().toString(), pack.getServerImageId(), FileTransferType.ChatImage, callback);
                break;
            }
            case ChatFile:{
                var pack = new ChatFileBroadcastPack(data);
                this.client.getRoomFrame().showFileUploadedMessage(
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
    }

    public void sendChatText(String text,IMessageSendCallback callback) throws PackageTooLargeException {
        var msgId = getMsgId(callback);
        var pack = new ChatTextSendPack(msgId,text);
        send(pack);
    }

    public void getHistory(){
        send(new QueryHistoryPack(0L));
    }

}
