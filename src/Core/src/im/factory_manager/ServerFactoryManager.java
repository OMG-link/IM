package im.factory_manager;

import im.protocol.fileTransfer.ServerFileReceiveTaskFactory;
import im.protocol.fileTransfer.ServerFileSendTaskFactory;

public class ServerFactoryManager {
    private final ServerFileSendTaskFactory fileSendTaskFactory = new ServerFileSendTaskFactory();
    private final ServerFileReceiveTaskFactory fileReceiveTaskFactory = new ServerFileReceiveTaskFactory();

    public ServerFileSendTaskFactory getFileSendTaskFactory() {
        return fileSendTaskFactory;
    }

    public ServerFileReceiveTaskFactory getFileReceiveTaskFactory() {
        return fileReceiveTaskFactory;
    }
}
