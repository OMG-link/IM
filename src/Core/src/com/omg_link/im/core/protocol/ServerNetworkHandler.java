package com.omg_link.im.core.protocol;

import com.omg_link.im.core.Server;
import com.omg_link.im.core.config.Config;
import com.omg_link.im.core.file_manager.NoSuchFileIdException;
import com.omg_link.im.core.message_manager.InvalidSerialIdException;
import com.omg_link.im.core.protocol.data.ByteArrayInfo;
import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;
import com.omg_link.im.core.protocol.data.PackageTooLargeException;
import com.omg_link.im.core.protocol.data_pack.DataPack;
import com.omg_link.im.core.protocol.data_pack.chat.ChatTextSendPack;
import com.omg_link.im.core.protocol.data_pack.chat.QueryHistoryPack;
import com.omg_link.im.core.protocol.data_pack.file_transfer.*;
import com.omg_link.im.core.protocol.data_pack.system.*;
import com.omg_link.im.core.protocol.data_pack.user_list.BroadcastUserListPack;
import com.omg_link.im.core.protocol.data_pack.user_list.SetRoomNamePack;
import com.omg_link.im.core.protocol.file_transfer.NoSuchTaskIdException;
import com.omg_link.im.core.protocol.file_transfer.ServerFileReceiveTask;
import com.omg_link.im.core.protocol.file_transfer.ServerFileSendTask;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.logging.Level;

public class ServerNetworkHandler implements Runnable {
    private final Server server;

    private ServerSocketChannel socketChannel;
    private Selector selector;

    private boolean isLoopingSelectionKeyList = false;
    private final Set<SelectionKey> selectionKeyList = new HashSet<>();

    public ServerNetworkHandler(Server server) {
        this.server = server;
        this.start();
    }

    public void start() {
        server.getLogger().log(
                Level.INFO,
                String.format("Starting server on port %d.\n", Config.getServerPort())
        );
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
                    e.printStackTrace();
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
            Attachment attachment = (Attachment) selectionKey.attachment();
            attachment.shouldDisconnect = true;
            attachment.onDisconnect();
            if (!isLoopingSelectionKeyList) {
                selectionKeyList.remove(selectionKey);
            }
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
            int packageLength = data.peekInt();
            if (packageLength > Config.packageMaxLength) {
                throw new InvalidPackageException();
            }
        }

        //Process data
        while (DataPack.canDecode(data)) {
            int packageLength = data.decodeInt();
            if (packageLength > Config.packageMaxLength) {
                throw new InvalidPackageException();
            }
            handlePacket(selectionKey, data.cut(packageLength));
        }

    }

    private void handlePacket(SelectionKey selectionKey, ByteData data) throws InvalidPackageException {
        Attachment attachment = (Attachment) selectionKey.attachment();

        DataPack.Type type = data.peekEnum(DataPack.Type.values());

        if (attachment.expectedReceiveType != null) {
            if (type != attachment.expectedReceiveType) {
                if (!attachment.isVersionChecked) { //兼容低版本
                    attachment.expectedSendType = DataPack.Type.CheckVersion;
                    attachment.expectedReceiveType = DataPack.Type.Undefined;
                    send(selectionKey, new CheckVersionPackV2());
                }
                return;
            }
        }

        switch (type) {
            case CheckVersion: {
                attachment.expectedSendType = DataPack.Type.CheckVersion;
                attachment.expectedReceiveType = DataPack.Type.Undefined;

                // CheckVersionPack for V1.3 (Server Version)
                try {
                    new CheckVersionPackV2(data.clone());
                    send(selectionKey, new CheckVersionPackV2());
                    break;
                } catch (InvalidPackageException ignored) {
                }

                // CheckVersionPack for V1.1
                try {
                    new CheckVersionPackV1(data.clone());
                    send(selectionKey, new CheckVersionPackV1());
                    break;
                } catch (InvalidPackageException ignored) {
                }

                // Default.
                // Used when a higher version of CheckVersionPack is sent.
                send(selectionKey, new CheckVersionPackV2());
                break;
            }
            case QueryHistory: {
                var pack = new QueryHistoryPack(data);
                try {
                    server.getMessageManager().sendHistory(selectionKey, pack.getLastSerialId());
                } catch (InvalidSerialIdException ignored) {
                }
                break;
            }
            case ChatText: {
                server.getMessageManager().processChatTextPack(
                        selectionKey,
                        new ChatTextSendPack(data)
                );
                break;
            }
            case ConnectRequest: {
                ConnectRequestPack pack = new ConnectRequestPack(data);
                if (pack.getUserName().length() > Config.nickMaxLength) return;
                if (attachment.isUserCreated()) return;

                attachment.expectedSendType = DataPack.Type.ConnectResult;
                attachment.expectedReceiveType = DataPack.Type.Undefined;

                ConnectResultPack connectResultPack;
                if (!Objects.equals(pack.getToken(), Config.getToken())) {
                    connectResultPack = new ConnectResultPack(ConnectResultPack.RejectReason.InvalidToken);
                } else {
                    attachment.user = server.getUserManager().createUser(pack.getUserName());
                    connectResultPack = new ConnectResultPack(attachment.user);
                }

                send(selectionKey, connectResultPack);
                break;
            }
            case FileUploadRequest: {
                UploadRequestPack requestPack = new UploadRequestPack(data);
                boolean ok;
                UploadReplyPack.Reason reason;
                UUID receiverTaskId = new UUID(0, 0);
                UUID receiverFileId = new UUID(0, 0);
                if (requestPack.getFileSize() > Config.fileMaxSize) {
                    ok = false;
                    reason = UploadReplyPack.Reason.fileTooLarge;
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
                                requestPack
                        );
                        receiverTaskId = task.getReceiverTaskId();
                        receiverFileId = task.getReceiverFileId();
                        ok = true;
                        reason = UploadReplyPack.Reason.ok;
                    } catch (IOException e) {
                        ok = false;
                        reason = UploadReplyPack.Reason.remoteIOError;
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
                    server.getLogger().log(
                            Level.INFO,
                            String.format("There is no ServerFileReceiveTask with UUID %s.", pack.getReceiverTaskId())
                    );
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
                    server.getLogger().log(
                            Level.INFO,
                            String.format("There is no ServerFileReceiveTask with UUID %s.", pack.getReceiverTaskId())
                    );
                }
                break;
            }
            case FileDownloadRequest: {
                DownloadRequestPack pack = new DownloadRequestPack(data);
                boolean ok;
                DownloadReplyPack.Reason reason;
                ServerFileSendTask task = null;
                try {
                    task = server.getFactoryManager().getFileSendTaskFactory().create(server, selectionKey, pack);
                    ok = true;
                    reason = DownloadReplyPack.Reason.ok;
                } catch (NoSuchFileIdException e) {
                    ok = false;
                    reason = DownloadReplyPack.Reason.fileNotFound;
                }
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
                break;
            }
            case FileUploadReply: {
                UploadReplyPack pack = new UploadReplyPack(data);
                try {
                    ServerFileSendTask task = server.getFactoryManager().getFileSendTaskFactory().find(pack.getSenderTaskId());
                    task.onReceiveUploadReply(pack);
                } catch (NoSuchTaskIdException e) {
                    server.getLogger().log(
                            Level.INFO,
                            String.format("There is no ServerFileSendTask with UUID %s.", pack.getSenderTaskId())
                    );
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
                    server.getLogger().log(
                            Level.INFO,
                            String.format("There is no ServerFileSendTask with UUID %s.", pack.getSenderTaskId())
                    );
                }
                break;
            }
            case Ping: {
                new PingPack(data);
                break;
            }
            default: {
                server.getLogger().log(Level.WARNING, "Server was unable to decode package type: " + type);
                throw new InvalidPackageException();
            }
        }

    }

    public void send(SelectionKey selectionKey, DataPack dataPack) throws PackageTooLargeException {
        Attachment attachment = (Attachment) selectionKey.attachment();

        if (attachment.shouldDisconnect) return;

        boolean isConnectionJustBuilt = false;

        if (attachment.expectedSendType != null) {
            if (dataPack.getType() != attachment.expectedSendType) {
                server.getLogger().log(
                        Level.WARNING,
                        String.format("Server wants to send package of type %s, but it is expected to send type %s. Package ignored.", dataPack.getType(), attachment.expectedSendType)
                );
                return;
            } else {
                switch (dataPack.getType()) {
                    case CheckVersion: {
                        attachment.isVersionChecked = true;
                        attachment.expectedSendType = DataPack.Type.Undefined;
                        attachment.expectedReceiveType = DataPack.Type.ConnectRequest;
                        break;
                    }
                    case ConnectResult: {
                        if (((ConnectResultPack) dataPack).isTokenAccepted()) {
                            attachment.expectedSendType = null;
                            attachment.expectedReceiveType = null;
                            isConnectionJustBuilt = true;
                        } else {
                            attachment.expectedSendType = DataPack.Type.Undefined;
                            attachment.expectedReceiveType = DataPack.Type.Undefined;
                        }
                        break;
                    }
                }
            }
        }

        try {
            send(selectionKey, dataPack.encode());
        } catch (IOException e) {
            disconnect(selectionKey);
            return;
        }
        if (isConnectionJustBuilt) {
            onConnectionBuilt(selectionKey);
        }
    }

    private void send(SelectionKey selectionKey, ByteData data) throws IOException {
        if (data.getLength() > Config.packageMaxLength) {
            throw new PackageTooLargeException();
        }

        SocketChannel socketChannel;
        try {
            socketChannel = (SocketChannel) selectionKey.channel();
        } catch (ClassCastException e) {
            server.getLogger().log(Level.WARNING, "Cannot cast selectionKey.channel() to SocketChannel.");
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

    public void broadcast(DataPack pack) {
        //Lock the selection key list
        isLoopingSelectionKeyList = true;
        for (SelectionKey selectionKey : selectionKeyList) {
            Attachment attachment = (Attachment) selectionKey.attachment();
            if (!attachment.isConnectionBuilt()) continue;
            send(selectionKey, pack);
        }
        isLoopingSelectionKeyList = false;
        //Disconnect all that should be disconnected
        Iterator<SelectionKey> iterator = selectionKeyList.iterator();
        while (iterator.hasNext()) {
            SelectionKey selectionKey = iterator.next();
            Attachment attachment = (Attachment) selectionKey.attachment();
            if (attachment.shouldDisconnect) {
                iterator.remove();
            }
        }
    }

    private void onConnectionBuilt(SelectionKey selectionKey) {
        Attachment attachment = (Attachment) selectionKey.attachment();
        attachment.expectedSendType = null;
        attachment.expectedReceiveType = null;
        try {
            send(selectionKey, new SetRoomNamePack());
            send(selectionKey, new BroadcastUserListPack(server.getUserManager().getUserList()));
        } catch (PackageTooLargeException e) {
            throw new RuntimeException(e);
        }
    }

}
