package protocol.helper;

import protocol.helper.data.ByteData;

public class Attachment {
    public ByteData data;
    public String userName;
    public long lastPackageTime;

    public Attachment(){
        data = new ByteData();
        userName = "anonymous";
        lastPackageTime = System.currentTimeMillis();
    }
}
