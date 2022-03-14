package im.factory_manager;

import im.protocol.fileTransfer.ClientFileReceiveTaskFactory;
import im.protocol.fileTransfer.ClientFileSendTaskFactory;

public class ClientFactoryManager {
    private final ClientFileSendTaskFactory fileSendTaskFactory = new ClientFileSendTaskFactory();
    private final ClientFileReceiveTaskFactory fileReceiveTaskFactory = new ClientFileReceiveTaskFactory();

    public ClientFileSendTaskFactory getFileSendTaskFactory() {
        return fileSendTaskFactory;
    }

    public ClientFileReceiveTaskFactory getFileReceiveTaskFactory() {
        return fileReceiveTaskFactory;
    }

}
