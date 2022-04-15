package com.omg_link.im.core.protocol.file_transfer;

import com.omg_link.im.core.ClientRoom;
import com.omg_link.im.core.gui.IFileTransferringPanel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientFileSendTaskFactory {
    private final Map<UUID,ClientFileSendTask> map = new HashMap<>();

    public ClientFileSendTask create(
            ClientRoom room,
            File file, FileTransferType fileTransferType,
            IFileTransferringPanel panel
    ) throws FileNotFoundException {
        UUID senderTaskId;
        do{
            senderTaskId = UUID.randomUUID();
        }while(map.containsKey(senderTaskId));
        ClientFileSendTask task = new ClientFileSendTask(
                room,senderTaskId,
                file,fileTransferType,
                panel
        );
        map.put(senderTaskId,task);
        return task;
    }

    public ClientFileSendTask find(UUID senderTaskId) throws NoSuchTaskIdException {
        if(!map.containsKey(senderTaskId)){
            throw new NoSuchTaskIdException();
        }
        return map.get(senderTaskId);
    }

    public void remove(ClientFileSendTask task) throws NoSuchTaskIdException {
        if(!map.containsKey(task.getSenderTaskId())){
            throw new NoSuchTaskIdException();
        }
        map.remove(task.getSenderTaskId());
    }

}
