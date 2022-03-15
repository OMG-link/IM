package im.protocol.fileTransfer;

import im.gui.IFileTransferringPanel;
import im.Client;
import im.protocol.data_pack.file_transfer.FileTransferType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientFileReceiveTaskFactory {
    private final Map<UUID,ClientFileReceiveTask> map = new HashMap<>();

    /**
     * @throws IOException When the file cannot be created.
     */
    public ClientFileReceiveTask create(
            Client handler,
            String fileName, UUID senderFileId, FileTransferType fileTransferType,
            IFileTransferringPanel panel, IDownloadCallback callback
    ) throws IOException {
        UUID receiverTaskId;
        do{
            receiverTaskId = UUID.randomUUID();
        }while(map.containsKey(receiverTaskId));
        ClientFileReceiveTask task = new ClientFileReceiveTask(
                handler,receiverTaskId,
                fileName,senderFileId,fileTransferType,
                panel,callback
        );
        map.put(receiverTaskId,task);
        return task;
    }

    public ClientFileReceiveTask find(UUID receiverTaskId) throws NoSuchTaskIdException {
        if(!map.containsKey(receiverTaskId)){
            throw new NoSuchTaskIdException();
        }
        return map.get(receiverTaskId);
    }

    public void remove(ClientFileReceiveTask task) throws NoSuchTaskIdException {
        if(!map.containsKey(task.getReceiverTaskId())){
            throw new NoSuchTaskIdException();
        }
        map.remove(task.getReceiverTaskId());
    }

}
