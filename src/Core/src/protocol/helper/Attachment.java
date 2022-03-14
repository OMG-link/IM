package protocol.helper;

import protocol.helper.data.ByteData;

public class Attachment {
    public ByteData receiveBuffer;
    public String userName;
    public boolean isVersionChecked = false;
    public boolean allowCommunication = false;

    public Attachment(){
        receiveBuffer = new ByteData();
        userName = "anonymous";
    }
}
