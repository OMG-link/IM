package protocol;

import IM.Config;
import IM.Server;
import mutil.file.WriteOnlyFile;
import mutil.uuidLocator.UuidConflictException;
import protocol.dataPack.*;
import protocol.helper.Attachment;
import protocol.helper.data.Data;
import protocol.helper.data.InvalidPackageException;
import protocol.helper.fileTransfer.FileReceiveTask;
import protocol.helper.fileTransfer.FileSendTask;
import protocol.helper.fileTransfer.ServerFileReceiveTask;
import protocol.helper.fileTransfer.ServerFileSendTask;

import javax.swing.*;
import java.io.FileNotFoundException;
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
    private final Server handler;

    private ServerSocketChannel socketChannel;
    private Selector selector;

    private final Queue<Data> records = new LinkedList<>();
    private final Set<SelectionKey> selectionKeyList = new HashSet<>();

    public ServerNetworkHandler(Server handler) {
        this.handler = handler;
    }

    public void start() {
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
            } catch (IOException ignored) {}
            selectionKeyList.remove(selectionKey);
            this.broadcastUserList();
        }
    }

    private void handleAccept(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        SelectionKey newSelectionKey = socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ, new Attachment());
        selectionKeyList.add(newSelectionKey);

        checkVersion(socketChannel);
        sendHistory(socketChannel);

    }

    private void handleRead(SelectionKey selectionKey) throws IOException,InvalidPackageException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        Attachment attachment = (Attachment) selectionKey.attachment();

        //log last communicate time
        attachment.lastPackageTime = System.currentTimeMillis();

        //Receive data
        Data data = attachment.data;
        Data tData = new Data();
        ByteBuffer byteBuffer = ByteBuffer.allocate(10 * 1024); //10KB

        int length = socketChannel.read(byteBuffer);
        if (length == -1) throw new IOException("Connection Closed.");
        byteBuffer.flip();
        tData.resize(byteBuffer.limit());
        byteBuffer.get(tData.getData(), 0, byteBuffer.limit());
        data.append(tData);

        //Avoid large package buffer
        if(data.length()>=4){
            int packageLength = Data.peekInt(data);
            if(packageLength > Config.packageMaxLength)
                throw new InvalidPackageException();
        }

        //Process data
        while (DataPack.canDecode(data)) {
            int packageLength = Data.decodeInt(data);
            if(packageLength>Config.packageMaxLength)
                throw new InvalidPackageException();
            handlePacket(selectionKey, data);
        }

    }

    private void handlePacket(SelectionKey selectionKey, Data data) throws IOException,InvalidPackageException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        Attachment attachment = (Attachment) selectionKey.attachment();

        DataPackType type;
        try {
            type = DataPackType.toType(Data.peekInt(data));
        } catch (InvalidParameterException e) {
            throw new InvalidPackageException();
        }

        switch (type) {
            case Text: {
                TextPack textPack = new TextPack(data);
                textPack.setStamp();

                Data newData = textPack.encode();
                addRecord(newData);
                broadcast(newData);
                break;
            }
            case NameUpdate: {
                NameUpdatePack nameUpdatePack = new NameUpdatePack(data);
                if(nameUpdatePack.getUserName().length()>=Config.nickMaxLength) return;
                attachment.userName = nameUpdatePack.getUserName();
                this.broadcastUserList();
                break;
            }
            case FileUploadRequest: {
                UploadRequestPack requestPack = new UploadRequestPack(data);
                UploadReplyPack replyPack;
                if (requestPack.getFileSize() > Config.fileMaxSize) {
                    replyPack = new UploadReplyPack(
                            requestPack.getUuid(),
                            false,
                            "File too large!"
                    );
                } else {
                    try {
                        WriteOnlyFile file = handler.getFileManager().createFile(
                                requestPack.getUuid(),
                                requestPack.getFileName(),
                                requestPack.getFileSize()
                        ).getWriteOnlyInstance();
                        /*
                         * Here is a hidden danger that the client may create lots of upload requests without finishing
                         * it, which may causes memory limit exceed and crashes the program.
                         * Maybe we need to clear unused upload requests in later version.
                         */
                        ServerFileReceiveTask task = new ServerFileReceiveTask(
                                handler,
                                file,
                                requestPack.getUuid(),
                                attachment.userName,
                                requestPack.getFileName(),
                                requestPack.getFileSize()
                        );
                        task.start();
                        replyPack = new UploadReplyPack(
                                requestPack.getUuid(),
                                true,
                                ""
                        );
                    } catch (UuidConflictException e) {
                        replyPack = new UploadReplyPack(
                                requestPack.getUuid(),
                                false,
                                "Unexpected UUID conflict."
                        );
                    }
                }
                this.send(socketChannel, replyPack);
                break;
            }
            case FileContent: {
                FileContentPack fileContentPack = new FileContentPack(data);
                Object o = handler.getUuidManager().get(fileContentPack.getUuid());
                if (o instanceof FileReceiveTask) {
                    FileReceiveTask task = (FileReceiveTask) o;
                    try {
                        if (fileContentPack.getOffset() < 0) {
                            task.end();
                        } else {
                            task.onDataReceived(fileContentPack.getData());
                        }
                    } catch (IOException e) {
                        task.end(e.toString());
                    }
                }
                break;
            }
            case FileDownloadRequest: {
                DownloadRequestPack pack = new DownloadRequestPack(data);
                try {
                    ServerFileSendTask task = new ServerFileSendTask(handler, socketChannel, pack.getUuid());
                    task.start();
                } catch (FileNotFoundException e) {
                    //do nothing
                }
                break;
            }
            case FileUploadReply: {
                UploadReplyPack pack = new UploadReplyPack(data);
                FileSendTask task = (FileSendTask) this.handler.getUuidManager().get(pack.getUuid());
                task.onReceiveUploadReply(pack.isOk(), pack.getDesc());
                break;
            }
            case Ping:{
                PingPack pack = new PingPack(data);
                break;
            }
            default: {
                Logger.getGlobal().log(Level.WARNING, "Server was unable to decode package type: " + type);
                throw new InvalidPackageException();
            }
        }

    }

    public void send(SocketChannel socketChannel, DataPack dataPack) throws IOException {
        send(socketChannel, dataPack.encode());
    }

    private void send(SocketChannel socketChannel, Data data) throws IOException {
        Data rawData = new Data();
        rawData.append(new Data(data.length()));
        rawData.append(data);

        ByteBuffer buffer = ByteBuffer.wrap(rawData.getData());
        buffer.position(rawData.length());
        buffer.flip();

        synchronized (socketChannel){
            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
        }
    }

    public void addRecord(Data data) {
        records.offer(data);
        if (records.size() > Config.recordsMaxLength) {
            records.poll();
        }
    }

    private void sendHistory(SocketChannel channel) throws IOException {
        for (Data data : records) {
            send(channel, data);
        }
    }

    private void broadcast(Data data) {
        Iterator<SelectionKey> iterator = selectionKeyList.iterator();
        while (iterator.hasNext()) {
            SelectionKey selectionKey = iterator.next();
            try {
                send((SocketChannel) selectionKey.channel(), data);
            } catch (IOException e) {
                iterator.remove();
            }
        }
    }

    public void broadcast(DataPack pack) {
        broadcast(pack.encode());
    }

    private void checkVersion(SocketChannel channel) throws IOException {
        CheckVersionPack pack = new CheckVersionPack();
        send(channel, pack);
    }

    private void broadcastUserList() {
        int length = selectionKeyList.size();
        String[] userList = new String[length];

        int i = 0;
        for (SelectionKey selectionKey : selectionKeyList) {
            Attachment attachment = (Attachment) selectionKey.attachment();
            userList[i++] = attachment.userName;
        }

        broadcast(new UserListPack(userList));

    }

}
