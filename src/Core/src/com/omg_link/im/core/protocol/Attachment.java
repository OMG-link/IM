package com.omg_link.im.core.protocol;

import com.omg_link.im.core.Server;
import com.omg_link.im.core.protocol.data.ByteData;
import com.omg_link.im.core.protocol.data_pack.DataPack;
import com.omg_link.im.core.user_manager.NoSuchUidException;
import com.omg_link.im.core.user_manager.User;

public class Attachment {
    private final Server server;

    public final ByteData receiveBuffer;
    public boolean isVersionChecked = false;
    public boolean shouldDisconnect = false;
    public DataPack.Type expectedSendType = DataPack.Type.Undefined;
    public DataPack.Type expectedReceiveType = DataPack.Type.CheckVersion;

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
