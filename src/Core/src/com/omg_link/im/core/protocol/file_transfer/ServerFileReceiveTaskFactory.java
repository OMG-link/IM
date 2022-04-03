package com.omg_link.im.core.protocol.file_transfer;

import com.omg_link.im.core.ServerRoom;
import com.omg_link.im.core.protocol.data_pack.file_transfer.UploadRequestPack;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerFileReceiveTaskFactory {
    private final Map<UUID,ServerFileReceiveTask> map = new HashMap<>();

    /**
     * @throws IOException When the file cannot be created.
     */
    public ServerFileReceiveTask create(
            ServerRoom handler, SelectionKey selectionKey,
            UploadRequestPack requestPack
    ) throws IOException {
        UUID receiverTaskId;
        do{
            receiverTaskId = UUID.randomUUID();
        }while(map.containsKey(receiverTaskId));
        ServerFileReceiveTask task = new ServerFileReceiveTask(
                handler,selectionKey,receiverTaskId,
                requestPack
        );
        map.put(receiverTaskId,task);
        return task;
    }

    public ServerFileReceiveTask find(UUID receiverTaskId) throws NoSuchTaskIdException {
        if(!map.containsKey(receiverTaskId)){
            throw new NoSuchTaskIdException();
        }
        return map.get(receiverTaskId);
    }

    public void remove(ServerFileReceiveTask task) throws NoSuchTaskIdException {
        if(!map.containsKey(task.getReceiverTaskId())){
            throw new NoSuchTaskIdException();
        }
        map.remove(task.getReceiverTaskId());
    }

}
