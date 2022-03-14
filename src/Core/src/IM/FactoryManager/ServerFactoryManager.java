package IM.FactoryManager;

import protocol.fileTransfer.ServerFileReceiveTaskFactory;
import protocol.fileTransfer.ServerFileSendTaskFactory;

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
