package im.protocol.fileTransfer;

import im.gui.IFileTransferringPanel;
import im.Client;
import im.protocol.dataPack.FileTransferType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientFileSendTaskFactory {
    private final Map<UUID,ClientFileSendTask> map = new HashMap<>();

    public ClientFileSendTask create(
            Client handler,
            File file, FileTransferType fileTransferType,
            IFileTransferringPanel panel, IUploadCallback callback
    ) throws FileNotFoundException {
        UUID senderTaskId;
        do{
            senderTaskId = UUID.randomUUID();
        }while(map.containsKey(senderTaskId));
        ClientFileSendTask task = new ClientFileSendTask(
                handler,senderTaskId,
                file,fileTransferType,
                panel,callback
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
