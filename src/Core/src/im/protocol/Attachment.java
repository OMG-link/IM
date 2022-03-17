package im.protocol;

import im.Server;
import im.protocol.data.ByteData;
import im.protocol.data_pack.DataPackType;
import im.user_manager.NoSuchUidException;
import im.user_manager.User;

public class Attachment {
    private final Server server;

    public final ByteData receiveBuffer;
    public boolean isVersionChecked = false;
    public DataPackType expectedSendType = DataPackType.Undefined;
    public DataPackType expectedReceiveType = DataPackType.CheckVersion;

    public User user;

    public Attachment(Server server){
        this.server = server;
        this.receiveBuffer = new ByteData();
        this.user = null;
    }

    public void onDisconnect(){
        try{
            if(user!=null){
                server.getUserManager().removeUser(user);
            }
        }catch (NoSuchUidException e){
            throw new RuntimeException(e);
        }
    }

    public boolean isUserCreated(){
        return user!=null;
    }

    public boolean isConnectionBuilt(){
        return expectedSendType==null;
    }

}
