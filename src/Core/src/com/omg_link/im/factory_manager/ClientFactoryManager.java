package com.omg_link.im.factory_manager;

import com.omg_link.im.protocol.file_transfer.ClientFileReceiveTaskFactory;
import com.omg_link.im.protocol.file_transfer.ClientFileSendTaskFactory;

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
