package protocol.helper.fileTransfer;

import IM.Server;
import mutil.file.WriteOnlyFile;
import mutil.uuidLocator.UUIDManager;
import protocol.dataPack.FileUploadedPack;

import java.util.UUID;

public class ServerFileReceiveTask extends FileReceiveTask{
    private final Server handler;
    private final String fileName;
    private final String sender;

    public ServerFileReceiveTask(Server handler,WriteOnlyFile file,UUID uuid, String sender,String fileName,long fileSize){
        super(uuid, file, fileSize);
        this.handler = handler;
        this.fileName = fileName;
        this.sender = sender;
    }

    @Override
    public UUIDManager getUuidManager() {
        return handler.getUuidManager();
    }

    @Override
    protected void onEndSuccess(){
        FileUploadedPack pack = new FileUploadedPack(sender,uuid,fileName, fileSize);
        handler.getNetworkHandler().broadcast(pack);
        handler.getNetworkHandler().addRecord(pack.encode());
    }

    @Override
    protected void onEndFail(){
        //do nothing
    }

}
