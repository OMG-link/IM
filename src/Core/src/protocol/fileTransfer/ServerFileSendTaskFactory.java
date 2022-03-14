package protocol.fileTransfer;

import IM.Server;
import mutils.file.NoSuchFileIdException;
import protocol.dataPack.DownloadRequestPack;

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
            Server handler, SelectionKey selectionKey,
            DownloadRequestPack requestPack
    ) throws NoSuchFileIdException {
        UUID senderTaskId;
        do{
            senderTaskId = UUID.randomUUID();
        }while(map.containsKey(senderTaskId));
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
