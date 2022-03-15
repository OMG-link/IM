package im.protocol;

import im.Server;
import im.protocol.data.ByteData;
import im.user_manager.NoSuchUidException;
import im.user_manager.User;

public class Attachment {
    private final Server server;

    public final ByteData receiveBuffer;
    public boolean isVersionChecked = false;
    public boolean allowCommunication = false;
    public boolean isUsernameSet = false;

    public User user;

    public Attachment(Server server){
        this.server = server;
        this.receiveBuffer = new ByteData();
        this.user = server.getUserManager().createUser();
    }

    public void onDisconnect(){
        try{
            server.getUserManager().removeUser(user);
        }catch (NoSuchUidException e){
            throw new RuntimeException(e);
        }
    }

}
