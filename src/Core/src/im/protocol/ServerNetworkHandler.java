package im.protocol;

import im.Server;
import im.config.Config;
import im.file_manager.NoSuchFileIdException;
import im.protocol.data.ByteArrayInfo;
import im.protocol.data_pack.*;
import im.protocol.data_pack.chat.ChatImagePack;
import im.protocol.data_pack.chat.TextPack;
import im.protocol.data_pack.file_transfer.*;
import im.protocol.data_pack.system.CheckVersionPack;
import im.protocol.data_pack.system.PingPack;
import im.protocol.data_pack.user_list.SetUidPack;
import im.protocol.data_pack.user_list.SetUsernamePack;
import im.protocol.data_pack.user_list.SetRoomNamePack;
import im.protocol.data_pack.user_list.BroadcastUserListPack;
import im.protocol.fileTransfer.NoSuchTaskIdException;
import im.protocol.fileTransfer.ServerFileReceiveTask;
import im.protocol.fileTransfer.ServerFileSendTask;
import im.protocol.data.ByteData;
import im.protocol.data.InvalidPackageException;
import im.protocol.data.PackageTooLargeException;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerNetworkHandler implements Runnable {
    private final Server server;

    private ServerSocketChannel socketChannel;
    private Selector selector;

    private final Queue<ByteData> records = new LinkedList<>();
    private final Set<SelectionKey> selectionKeyList = new HashSet<>();

    public ServerNetworkHandler(Server server) {
        this.server = server;
    }

    public void start() {
        System.out.printf("Starting server on port %d.\n", Config.getServerPort());
        try {
            this.selector = Selector.open();
            this.socketChannel = ServerSocketChannel.open();

            this.socketChannel.bind(new InetSocketAddress(Config.getServerPort()));
            this.socketChannel.configureBlocking(false);
            this.socketChannel.register(selector, SelectionKey.OP_ACCEPT);

            Thread thread = new Thread(this, "Server Receive Thread");
            thread.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to start server.");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void end() {
        try {
            if (this.socketChannel != null)
                this.socketChannel.close();
        } catch (IOException e) {
            //do nothing
        }
        try {
            if (this.selector != null)
                this.selector.close();
        } catch (IOException e) {
            //do nothing
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                this.selector.select();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            Iterator<SelectionKey> it = this.selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey selectionKey = it.next();
                it.remove();
                try {
                    if (selectionKey.isAcceptable()) {
                        this.handleAccept(selectionKey);
                    }
                    if (selectionKey.isReadable()) {
                        this.handleRead(selectionKey);
                    }
                } catch (IOException | InvalidPackageException e) {
                    disconnect(selectionKey);
                }
            }
        }
        this.end();
    }

    private void disconnect(SelectionKey selectionKey) {
        if (selectionKey != null && selectionKey.isValid()) {
            try {
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                socketChannel.close();
            } catch (IOException ignored) {
            }
            selectionKeyList.remove(selectionKey);
            Attachment attachment = (Attachment) selectionKey.attachment();
            attachment.onDisconnect();
        }
    }

    private void handleAccept(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        SelectionKey newSelectionKey = socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ, new Attachment(server));
        selectionKeyList.add(newSelectionKey);

    }

    private void handleRead(SelectionKey selectionKey) throws IOException, InvalidPackageException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        Attachment attachment = (Attachment) selectionKey.attachment();

        //Receive data
        ByteData data = attachment.receiveBuffer;
        ByteData tData = new ByteData();
        ByteBuffer byteBuffer = ByteBuffer.allocate(10 * 1024); //10KB

        int length = socketChannel.read(byteBuffer);
        if (length == -1) throw new IOException("Connection Closed.");
        byteBuffer.flip();
        tData.setLength(byteBuffer.limit());

        ByteArrayInfo tDataInfo = tData.getData();
        byteBuffer.get(tDataInfo.getArray(), tDataInfo.getOffset(), tDataInfo.getLength());
        data.append(tData);

        //Avoid large package buffer
        if (data.getLength() >= 4) {
            int packageLength = ByteData.peekInt(data);
            if (packageLength > Config.packageMaxLength) {
                throw new InvalidPackageException();
            }
        }

        //Process data
        while (DataPack.canDecode(data)) {
            int packageLength = ByteData.decodeInt(data);
            if (packageLength > Config.packageMaxLength) {
                throw new InvalidPackageException();
            }
            handlePacket(selectionKey, data);
        }

    }

    private void handlePacket(SelectionKey selectionKey, ByteData data) throws IOException, InvalidPackageException {
        Attachment attachment = (Attachment) selectionKey.attachment();

        DataPackType type;
        try {
            type = DataPackType.toType(ByteData.peekInt(data));
        } catch (InvalidParameterException e) {
            throw new InvalidPackageException();
        }

        //Old version does not send CheckVersionPack to server.
        if (type != DataPackType.CheckVersion) {
            if (!attachment.isVersionChecked) { //Old version
                attachment.isVersionChecked = true;
                doVersionCheck(selectionKey, new CheckVersionPack());
                return;
            }
            if (!attachment.allowCommunication) {
                return;
            }
        }

        switch (type) {
            case CheckVersion: {
                attachment.isVersionChecked = true;
                //CheckVersionPack for V1.3 (Server Version)
                try {
                    CheckVersionPack pack = new CheckVersionPack(data);
                    if (Objects.equals(pack.getCompatibleVersion(), Config.compatibleVersion)) {
                        attachment.allowCommunication = true;
                    }
                    doVersionCheck(selectionKey, new CheckVersionPack());
                    if (attachment.allowCommunication) {
                        onVersionChecked(selectionKey);
                    }
                    break;
                } catch (InvalidPackageException ignored) {
                }
                //Default. Used when a higher version of CheckVersionPack is sent.
                doVersionCheck(selectionKey, new CheckVersionPack());
                break;
            }
            case Text: {
                TextPack pack = new TextPack(data);
                pack.setStamp();

                broadcast(pack, true);
                break;
            }
            case SetUserName: {
                SetUsernamePack pack = new SetUsernamePack(data);
                if (pack.getUserName().length() >= Config.nickMaxLength) return;
                //For safety reasons, users are not allowed to change their name more than once.
                if (attachment.isUsernameSet) return;
                attachment.isUsernameSet = true;
                attachment.user.setName(pack.getUserName());
                break;
            }
            case FileUploadRequest: {
                UploadRequestPack requestPack = new UploadRequestPack(data);
                boolean ok;
                String reason;
                UUID receiverTaskId = new UUID(0, 0);
                UUID receiverFileId = new UUID(0, 0);
                if (requestPack.getFileSize() > Config.fileMaxSize) {
                    ok = false;
                    reason = "File too large!";
                } else {
                    try {
                        /*
                         * Here is a hidden danger that the client may create lots of upload requests without finishing
                         * it, which may causes memory limit exceed and crashes the program.
                         * Maybe we need to clear unused upload requests in later version.
                         */
                        ServerFileReceiveTask task = server.getFactoryManager().getFileReceiveTaskFactory().create(
                                server,
                                selectionKey,
                                requestPack,
                                attachment.user.getName()
                        );
                        receiverTaskId = task.getReceiverTaskId();
                        receiverFileId = task.getReceiverFileId();
                        ok = true;
                        reason = "";
                    } catch (IOException e) {
                        ok = false;
                        reason = "Can not create file on server.";
                    }
                }
                try {
                    this.send(selectionKey, new UploadReplyPack(
                            requestPack,
                            receiverTaskId,
                            receiverFileId,
                            ok,
                            reason
                    ));
                } catch (PackageTooLargeException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case FileUploadFinish: {
                UploadFinishPack pack = new UploadFinishPack(data);
                try {
                    ServerFileReceiveTask task = server.getFactoryManager().getFileReceiveTaskFactory().find(pack.getReceiverTaskId());
                    task.end();
                } catch (NoSuchTaskIdException e) {
                    Logger.getLogger("Server").log(Level.INFO, String.format("There is no ServerFileReceiveTask with UUID %s.", pack.getReceiverTaskId()));
                    try {
                        send(selectionKey, new UploadResultPack(pack.getSenderTaskId()));
                    } catch (PackageTooLargeException e2) {
                        throw new RuntimeException(e2);
                    }
                }
                break;
            }
            case FileContent: {
                FileContentPack pack = new FileContentPack(data);
                try {
                    ServerFileReceiveTask task = server.getFactoryManager().getFileReceiveTaskFactory().find(pack.getReceiverTaskId());
                    try {
                        task.onDataReceived(pack.getData());
                    } catch (IOException e) {
                        task.onEndFailed(e.toString());
                    }
                } catch (NoSuchTaskIdException e) {
                    Logger.getLogger("Server").log(Level.INFO, String.format("There is no ServerFileReceiveTask with UUID %s.", pack.getReceiverTaskId()));
                }
                break;
            }
            case FileDownloadRequest: {
                DownloadRequestPack pack = new DownloadRequestPack(data);
                boolean ok;
                String reason;
                ServerFileSendTask task = null;
                try {
                    task = server.getFactoryManager().getFileSendTaskFactory().create(server, selectionKey, pack);
                    ok = true;
                    reason = "";
                } catch (NoSuchFileIdException e) {
                    ok = false;
                    reason = String.format("No such file.{UUID=%s}.", pack.getSenderFileId());
                }
                try {
                    this.send(selectionKey, new DownloadReplyPack(
                            pack,
                            task == null ? new UUID(0, 0) : task.getSenderTaskId(),
                            ok,
                            reason
                    ));
                    if (ok) { //UploadRequestPack should be sent after DownloadReplyPack
                        assert task != null;
                        task.start();
                    }
                } catch (PackageTooLargeException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            case FileUploadReply: {
                UploadReplyPack pack = new UploadReplyPack(data);
                try {
                    ServerFileSendTask task = server.getFactoryManager().getFileSendTaskFactory().find(pack.getSenderTaskId());
                    task.onReceiveUploadReply(pack);
                } catch (NoSuchTaskIdException e) {
                    Logger.getLogger("Server").log(Level.INFO, String.format("There is no ServerFileSendTask with UUID %s.", pack.getSenderTaskId()));
                }
                break;
            }
            case FileUploadResult: {
                UploadResultPack pack = new UploadResultPack(data);
                try {
                    ServerFileSendTask task = server.getFactoryManager().getFileSendTaskFactory().find(pack.getSenderTaskId());
                    if (pack.isOk()) {
                        task.onEndSucceed();
                    } else {
                        task.onEndFailed(pack.getReason());
                    }
                } catch (NoSuchTaskIdException e) {
                    Logger.getLogger("Server").log(Level.INFO, String.format("There is no ServerFileSendTask with UUID %s.", pack.getSenderTaskId()));
                }
                break;
            }
            case ChatImage: {
                ChatImagePack pack = new ChatImagePack(data);
                broadcast(pack, true);
                break;
            }
            case Ping: {
                PingPack pack = new PingPack(data);
                break;
            }
            default: {
                Logger.getGlobal().log(Level.WARNING, "Server was unable to decode package type: " + type);
                throw new InvalidPackageException();
            }
        }

    }

    public void send(SelectionKey selectionKey, DataPack dataPack) throws IOException, PackageTooLargeException {
        if (dataPack.getType() != DataPackType.CheckVersion && !((Attachment) selectionKey.attachment()).allowCommunication) {
            return;
        }
        send(selectionKey, dataPack.encode());
    }

    private void send(SelectionKey selectionKey, ByteData data) throws IOException, PackageTooLargeException {
        if (data.getLength() > Config.packageMaxLength) {
            throw new PackageTooLargeException();
        }

        SocketChannel socketChannel;
        try {
            socketChannel = (SocketChannel) selectionKey.channel();
        } catch (ClassCastException e) {
            Logger.getLogger("IMServer").log(Level.WARNING, "Cannot cast selectionKey.channel() to SocketChannel.");
            return;
        }

        ByteData rawData = new ByteData();
        rawData.append(ByteData.encode(data.getLength()));
        rawData.append(data);

        ByteArrayInfo rawDataInfo = rawData.getData();
        ByteBuffer buffer = ByteBuffer.wrap(rawDataInfo.getArray(), rawDataInfo.getOffset(), rawDataInfo.getLength());
        buffer.position(rawData.getLength());
        buffer.flip();

        synchronized (selectionKey.attachment()) {
            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
        }
    }

    public void addRecord(ByteData data) {
        records.offer(data);
        if (records.size() > Config.recordsMaxLength) {
            records.poll();
        }
    }

    private void sendHistory(SelectionKey selectionKey) throws IOException {
        for (ByteData data : records) {
            try {
                send(selectionKey, data);
            } catch (PackageTooLargeException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void broadcast(DataPack pack, boolean shouldAddToHistory) {
        broadcast(pack.encode(), shouldAddToHistory);
    }

    private void broadcast(ByteData data, boolean shouldAddToHistory) {
        if (data.getLength() > Config.packageMaxLength) {
            Logger.getLogger("Server").log(Level.WARNING, "Package too large! It will not be broadcast.");
            return;
        }
        if (shouldAddToHistory) {
            addRecord(data);
        }
        Iterator<SelectionKey> iterator = selectionKeyList.iterator();
        while (iterator.hasNext()) {
            SelectionKey selectionKey = iterator.next();
            try {
                send(selectionKey, data);
            } catch (IOException e) {
                iterator.remove();
            } catch (PackageTooLargeException e) {
                //This should have been checked before.
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Reply a version check pack to the client.
     *
     * @param selectionKey SelectionKey for the client.
     * @param replyPack    CheckVersionPack to reply.
     * @throws IOException If the package cannot be sent.
     */
    private void doVersionCheck(SelectionKey selectionKey, DataPack replyPack) throws IOException {
        try {
            send(selectionKey, replyPack);
        } catch (PackageTooLargeException e) {
            throw new RuntimeException(e);
        }
    }

    private void onVersionChecked(SelectionKey selectionKey) throws IOException {
        Attachment attachment = (Attachment) selectionKey.attachment();
        try {
            sendHistory(selectionKey);
            send(selectionKey, new SetRoomNamePack());
            send(selectionKey, new SetUidPack(attachment.user));
            send(selectionKey, new BroadcastUserListPack(server.getUserManager().getUserList()));
        } catch (PackageTooLargeException e) {
            throw new RuntimeException(e);
        }
    }

}
