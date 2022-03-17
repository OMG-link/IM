package im.protocol.data_pack;

import java.security.InvalidParameterException;

public enum DataPackType {
    Undefined,
    //System
    CheckVersion, Ping,
    //Chat
    Text, ChatImage, FileUploaded,
    //User list
    ConnectRequest, ConnectResult, SetRoomName,
    BroadcastUserList, BroadcastUserJoin, BroadcastUserLeft, BroadcastUserNameChanged,
    //File transfer
    FileUploadRequest, FileUploadReply, FileUploadFinish, FileUploadResult, FileDownloadRequest, FileDownloadReply, FileContent;

    public int toId() {
        return DataPackType.toId(this);
    }

    public static int toId(DataPackType type) {
        return type.ordinal();
    }

    public static DataPackType toType(int id) {
        if (id < 0 || id >= values().length) {
            throw new InvalidParameterException(String.format("Invalid data pack type %d.", id));
        }
        return values()[id];
    }

}
