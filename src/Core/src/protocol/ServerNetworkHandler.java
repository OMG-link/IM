package protocol;

import IM.Config;
import IM.Server;
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
        System.out.printf("Starting server on port %d.\n",Config.getServerPort());
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
            this.broadcastUserList();
        }
    }

    private void handleAccept(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        SelectionKey newSelectionKey = socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ, new Attachment());
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

        //Old version does not send CheckVersionPack to server.
        if(type!=DataPackType.CheckVersion){
            if(!attachment.isVersionChecked){ //Old version
                attachment.isVersionChecked = true;
                doVersionCheck(selectionKey,new CheckVersionPack());
                return;
            }
            if(!attachment.allowCommunication){
                return;
            }
        }

        switch (type) {
            case CheckVersion:{
                attachment.isVersionChecked = true;
                //CheckVersionPack for V1.3 (Server Version)
                try{
                    CheckVersionPack pack = new CheckVersionPack(data);
                    if(Objects.equals(pack.getCompatibleVersion(), Config.compatibleVersion)){
                        attachment.allowCommunication = true;
                    }
                    doVersionCheck(selectionKey,new CheckVersionPack());
                    if(attachment.allowCommunication){
                        onVersionChecked(selectionKey);
                    }
                    break;
                }catch (InvalidPackageException ignored){
                }
                //Default. Used when a higher version of CheckVersionPack is sent.
                doVersionCheck(selectionKey,new CheckVersionPack());
                break;
            }
            case Text: {
                TextPack textPack = new TextPack(data);
                textPack.setStamp();

                broadcast(textPack,true);
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
                String reason;
                UUID receiverTaskId = new UUID(0,0);
                UUID receiverFileId = new UUID(0,0);
                if (requestPack.getFileSize() > Config.fileMaxSize) {
                    ok = false;
                    reason = "File too large!";
                } else {
                    try{
                        /*
                         * Here is a hidden danger that the client may create lots of upload requests without finishing
                         * it, which may causes memory limit exceed and crashes the program.
                         * Maybe we need to clear unused upload requests in later version.
                         */
                        ServerFileReceiveTask task = new ServerFileReceiveTask(
                                handler,
                                selectionKey,
                                requestPack,
                                attachment.userName
                        );
                        receiverTaskId = task.getReceiverTaskId();
                        receiverFileId = task.getReceiverFileId();
                        ok = true;
                        reason = "";
                    }catch (IOException e){
                        ok = false;
                        reason = "Can not create file on server.";
                    }
                }
                try{
                    this.send(selectionKey, new UploadReplyPack(
                            requestPack,
                            receiverTaskId,
                            receiverFileId,
                            ok,
                            reason
                    ));
                }catch (PackageTooLargeException e){
                    throw new RuntimeException(e);
                }
                break;
            }
            case FileUploadFinish:{
                UploadFinishPack pack = new UploadFinishPack(data);
                Object o = handler.getUuidManager().get(pack.getReceiverTaskId());
                if(o instanceof ServerFileReceiveTask){
                    ServerFileReceiveTask task = (ServerFileReceiveTask) o;
                    task.end();
                }else{
                    try{
                        send(selectionKey,new UploadResultPack(pack.getSenderTaskId()));
                    }catch (PackageTooLargeException e){
                        throw new RuntimeException(e);
                    }
                }
                break;
            }
            case FileContent: {
                FileContentPack fileContentPack = new FileContentPack(data);
                Object o = handler.getUuidManager().get(fileContentPack.getReceiverTaskId());
                if (o instanceof FileReceiveTask) {
                    FileReceiveTask task = (FileReceiveTask) o;
                    try {
                        task.onDataReceived(fileContentPack.getData());
                    } catch (IOException e) {
                        task.onEndFailed(e.toString());
                    }
                }
                break;
            }
            case FileDownloadRequest: {
                DownloadRequestPack pack = new DownloadRequestPack(data);
                boolean ok;
                String reason;
                ServerFileSendTask task = null;
                try {
                    task = new ServerFileSendTask(handler, selectionKey, pack);
                    ok = true;
                    reason = "";
                } catch (FileNotFoundException e) {
                    ok = false;
                    reason = String.format("No such file.{UUID=%s}.",pack.getSenderFileId());
                }
                try{
                    this.send(selectionKey,new DownloadReplyPack(
                            pack,
                            task==null?new UUID(0,0):task.getSenderTaskId(),
                            ok,
                            reason
                    ));
                    if(ok){ //UploadRequestPack should be sent after DownloadReplyPack
                        task.start();
                    }
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
                    task.onReceiveUploadReply(pack);
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
        if(dataPack.getType()!=DataPackType.CheckVersion&&!((Attachment)selectionKey.attachment()).allowCommunication){
            return;
        }
        send(selectionKey, dataPack.encode());
    }

    private void send(SelectionKey selectionKey, ByteData data) throws IOException, PackageTooLargeException {
        if(data.getData().length>Config.packageMaxLength){
            throw new PackageTooLargeException();
        }

        SocketChannel socketChannel;
        try{
            socketChannel = (SocketChannel) selectionKey.channel();
        }catch (ClassCastException e){
            Logger.getLogger("IMServer").log(Level.WARNING,"Cannot cast selectionKey.channel() to SocketChannel.");
            return;
        }

        ByteData rawData = new ByteData();
        rawData.append(new ByteData(data.length()));
        rawData.append(data);

        ByteBuffer buffer = ByteBuffer.wrap(rawData.getData());
        buffer.position(rawData.length());
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
            try{
                send(selectionKey, data);
            }catch (PackageTooLargeException e){
                throw new RuntimeException(e);
            }
        }
    }

    public void broadcast(DataPack pack,boolean shouldAddToHistory) {
        broadcast(pack.encode(),shouldAddToHistory);
    }

    private void broadcast(ByteData data,boolean shouldAddToHistory) {
        if(data.getData().length>Config.packageMaxLength){
            Logger.getLogger("Server").log(Level.WARNING,"Package too large! It will not be broadcast.");
            return;
        }
        if(shouldAddToHistory){
            addRecord(data);
        }
        Iterator<SelectionKey> iterator = selectionKeyList.iterator();
        while (iterator.hasNext()) {
            SelectionKey selectionKey = iterator.next();
            try {
                send(selectionKey, data);
            } catch (IOException e) {
                iterator.remove();
            } catch (PackageTooLargeException e){
                //This should have been checked before.
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Reply a version check pack to the client.
     * @param selectionKey SelectionKey for the client.
     * @param replyPack CheckVersionPack to reply.
     * @throws IOException If the package cannot be sent.
     */
    private void doVersionCheck(SelectionKey selectionKey,DataPack replyPack) throws IOException {
        try{
            send(selectionKey,replyPack);
        }catch (PackageTooLargeException e){
            throw new RuntimeException(e);
        }
    }

    private void onVersionChecked(SelectionKey selectionKey) throws IOException {
        sendHistory(selectionKey);

        try{
            send(selectionKey,new RoomNamePack());
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

        broadcast(new UserListPack(userList),false);

    }

}
