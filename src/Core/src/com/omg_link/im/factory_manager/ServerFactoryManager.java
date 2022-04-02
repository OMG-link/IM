package com.omg_link.im.factory_manager;

import com.omg_link.im.protocol.file_transfer.ServerFileReceiveTaskFactory;
import com.omg_link.im.protocol.file_transfer.ServerFileSendTaskFactory;

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
