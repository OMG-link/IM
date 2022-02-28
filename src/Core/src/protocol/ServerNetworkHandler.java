package protocol;

import IM.Config;
import IM.Server;
import mutil.file.WriteOnlyFile;
import mutil.uuidLocator.UuidConflictException;
import protocol.dataPack.*;
import protocol.helper.Attachment;
import protocol.helper.data.ByteData;
import protocol.helper.data.InvalidPackageException;
import protocol.helper.data.PackageTooLargeException;
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

    private final Queue<ByteData> records = new LinkedList<>();
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
            } catch (IOException ignored) {
            }
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

    private void handleRead(SelectionKey selectionKey) throws IOException, InvalidPackageException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        Attachment attachment = (Attachment) selectionKey.attachment();

        //log last communicate time
        attachment.lastPackageTime = System.currentTimeMillis();

        //Receive data
        ByteData data = attachment.data;
        ByteData tData = new ByteData();
        ByteBuffer byteBuffer = ByteBuffer.allocate(10 * 1024); //10KB

        int length = socketChannel.read(byteBuffer);
        if (length == -1) throw new IOException("Connection Closed.");
        byteBuffer.flip();
        tData.resize(byteBuffer.limit());
        byteBuffer.get(tData.getData(), 0, byteBuffer.limit());
        data.append(tData);

        //Avoid large package buffer
        if (data.length() >= 4) {
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
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        Attachment attachment = (Attachment) selectionKey.attachment();

        DataPackType type;
        try {
            type = DataPackType.toType(ByteData.peekInt(data));
        } catch (InvalidParameterException e) {
            throw new InvalidPackageException();
        }

        switch (type) {
            case Text: {
                TextPack textPack = new TextPack(data);
                textPack.setStamp();

                ByteData newData = textPack.encode();
                broadcast(newData);
                break;
            }
            case NameUpdate: {
                NameUpdatePack nameUpdatePack = new NameUpdatePack(data);
                if (nameUpdatePack.getUserName().length() >= Config.nickMaxLength) return;
                attachment.userName = nameUpdatePack.getUserName();
                this.broadcastUserList();
                break;
            }
            case FileUploadRequest: {
                UploadRequestPack requestPack = new UploadRequestPack(data);
                boolean ok;
                String desc;
                UUID receiverTaskId = new UUID(0,0);
                if (requestPack.getFileSize() > Config.fileMaxSize) {
                    ok = false;
                    desc = "File too large!";
                } else {
                    try {
                        WriteOnlyFile file = handler.getFileManager().createFile(requestPack.getFileName()).getWriteOnlyInstance();
                        /*
                         * Here is a hidden danger that the client may create lots of upload requests without finishing
                         * it, which may causes memory limit exceed and crashes the program.
                         * Maybe we need to clear unused upload requests in later version.
                         */
                        ServerFileReceiveTask task = new ServerFileReceiveTask(
                                handler,
                                socketChannel,
                                file,
                                attachment.userName,
                                requestPack.getFileName(),
                                requestPack.getFileSize(),
                                requestPack.getFileTransferType()
                        );
                        task.setSenderTaskId(requestPack.getSenderTaskId());
                        receiverTaskId = task.getUuid();
                        ok = true;
                        desc = "";
                    } catch (UuidConflictException e) {
                        ok = false;
                        desc = "Unexpected UUID conflict.";
                    }
                }
                try{
                    this.send(socketChannel, new UploadReplyPack(
                            requestPack.getSenderTaskId(),
                            receiverTaskId,
                            ok,
                            desc,
                            requestPack.getFileTransferType()
                    ));
                }catch (PackageTooLargeException e){
                    throw new RuntimeException(e);
                }
                break;
            }
            case FileContent: {
                FileContentPack fileContentPack = new FileContentPack(data);
                Object o = handler.getUuidManager().get(fileContentPack.getReceiverTaskId());
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
                boolean ok;
                String reason;
                try {
                    ServerFileSendTask task = new ServerFileSendTask(handler, socketChannel, pack.getFileId(), pack.getFileTransferType());
                    task.setReceiverTaskId(pack.getReceiverTaskId());
                    task.start();
                    ok = true;
                    reason = "";
                } catch (FileNotFoundException e) {
                    ok = false;
                    reason = String.format("No such file.{UUID=%s}.",pack.getFileId());
                }
                try{
                    this.send(socketChannel,new DownloadReplyPack(
                            pack.getReceiverTaskId(),
                            ok,
                            reason,
                            pack.getFileTransferType()
                    ));
                }catch (PackageTooLargeException e){
                    throw new RuntimeException(e);
                }
                break;
            }
            case FileUploadReply: {
                UploadReplyPack pack = new UploadReplyPack(data);
                Object o = this.handler.getUuidManager().get(pack.getSenderTaskId());
                if(o instanceof FileSendTask){
                    FileSendTask task = (FileSendTask) o;
                    task.onReceiveUploadReply(pack.isOk(), pack.getDesc());
                }
                break;
            }
            case FileUploadResult: {
                UploadResultPack pack = new UploadResultPack(data);
                Object o = this.handler.getUuidManager().get(pack.getSenderTaskId());
                if(o instanceof FileSendTask){
                    FileSendTask task = (FileSendTask) o;
                    if(pack.isOk()){
                        task.onEndSucceed();
                    }else{
                        task.onEndFailed(pack.getReason());
                    }
                }
                break;
            }
            case ChatImage:{
                ChatImagePack pack = new ChatImagePack(data);
                broadcast(pack);
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

    public void send(SocketChannel socketChannel, DataPack dataPack) throws IOException, PackageTooLargeException {
        send(socketChannel, dataPack.encode());
    }

    private void send(SocketChannel socketChannel, ByteData data) throws IOException, PackageTooLargeException {
        if(data.getData().length>Config.packageMaxLength){
            throw new PackageTooLargeException();
        }

        ByteData rawData = new ByteData();
        rawData.append(new ByteData(data.length()));
        rawData.append(data);

        ByteBuffer buffer = ByteBuffer.wrap(rawData.getData());
        buffer.position(rawData.length());
        buffer.flip();

        synchronized (socketChannel) {
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

    private void sendHistory(SocketChannel channel) throws IOException {
        for (ByteData data : records) {
            try{
                send(channel, data);
            }catch (PackageTooLargeException e){
                throw new RuntimeException(e);
            }
        }
    }

    private void broadcast(ByteData data) {
        if(data.getData().length>Config.packageMaxLength){
            Logger.getLogger("Server").log(Level.WARNING,"Package too large! It will not be broadcast.");
            return;
        }
        addRecord(data);
        Iterator<SelectionKey> iterator = selectionKeyList.iterator();
        while (iterator.hasNext()) {
            SelectionKey selectionKey = iterator.next();
            try {
                send((SocketChannel) selectionKey.channel(), data);
            } catch (IOException e) {
                iterator.remove();
            } catch (PackageTooLargeException e){
                //This should have been checked before.
                throw new RuntimeException(e);
            }
        }
    }

    public void broadcast(DataPack pack) {
        broadcast(pack.encode());
    }

    private void checkVersion(SocketChannel channel) throws IOException {
        CheckVersionPack pack = new CheckVersionPack();
        try{
            send(channel, pack);
        }catch (PackageTooLargeException e){
            throw new RuntimeException(e);
        }
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
