package com.omg_link.im.core.protocol.data_pack;

import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data.InvalidPackageException;

public abstract class DataPack {

    public enum Type {
        Undefined,
        //System: CheckVersion must have value=1
        CheckVersion, Ping, ConnectRequest, ConnectResult,
        //Chat
        QueryHistory, ChatHistory,
        SelfSentNotice, ChatText, ChatImage, ChatFile,
        //User list
        SetRoomName, SetAvatar,
        BroadcastUserList, BroadcastUserJoin, BroadcastUserLeft, BroadcastUserNameChanged,
        //File transfer
        FileUploadRequest, FileUploadReply, FileUploadFinish, FileUploadResult, FileDownloadRequest, FileDownloadReply, FileContent

    }

    private final Type type;

    public DataPack(Type type){
        this.type = type;
    }

    public DataPack(ByteData data) throws InvalidPackageException {
        this.type = data.decodeEnum(Type.values());
    }

    public ByteData encode(){
        return ByteData.encode(type);
    }

    public Type getType() {
        return this.type;
    }

    public static boolean canDecode(ByteData data){
        try{
            int length = data.peekInt();
            return data.getLength()>=length+4;
        }catch(InvalidPackageException e){
            return false;
        }
    }

}
