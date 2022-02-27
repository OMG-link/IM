package protocol.helper;

import protocol.helper.data.Data;

public class Attachment {
    public Data data;
    public String userName;
    public long lastPackageTime;

    public Attachment(){
        data = new Data();
        userName = "anonymous";
        lastPackageTime = System.currentTimeMillis();
    }
}
