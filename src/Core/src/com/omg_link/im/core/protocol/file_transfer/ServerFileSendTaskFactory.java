package com.omg_link.im.core.protocol.file_transfer;

import com.omg_link.im.core.ServerRoom;
import com.omg_link.im.core.file_manager.NoSuchFileIdException;
import com.omg_link.im.core.protocol.data_pack.file_transfer.DownloadRequestPack;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerFileSendTaskFactory {
    private final Map<UUID,ServerFileSendTask> map = new HashMap<>();

    /**
     * @throws NoSuchFileIdException When the senderFileId declared in request pack is invalid.
     */
    public ServerFileSendTask create(
            ServerRoom handler, SelectionKey selectionKey,
            DownloadRequestPack requestPack
    ) throws NoSuchFileIdException {
        UUID senderTaskId;
        do{
            senderTaskId = UUID.randomUUID();
        }while(map.containsKey(senderTaskId)); //存在线程安全问题，懒得管了
        ServerFileSendTask task = new ServerFileSendTask(
                handler,selectionKey,senderTaskId,
                requestPack
        );
        map.put(senderTaskId,task);
        return task;
    }

    public ServerFileSendTask find(UUID senderTaskId) throws NoSuchTaskIdException {
        if(!map.containsKey(senderTaskId)){
            throw new NoSuchTaskIdException();
        }
        return map.get(senderTaskId);
    }

    public void remove(ServerFileSendTask task) throws NoSuchTaskIdException {
        if(!map.containsKey(task.getSenderTaskId())){
            throw new NoSuchTaskIdException();
        }
        map.remove(task.getSenderTaskId());
    }

}
