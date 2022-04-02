package com.omg_link.im.protocol.data_pack.system;

import com.omg_link.im.protocol.data.ByteData;
import com.omg_link.im.protocol.data.InvalidPackageException;
import com.omg_link.im.protocol.data_pack.DataPack;

public class ConnectRequestPack extends DataPack {
    private final String userName;
    private final String token;

    public ConnectRequestPack(String userName, String token){
        super(Type.ConnectRequest);
        this.userName = userName;
        this.token = token;
    }

    public ConnectRequestPack(ByteData data) throws InvalidPackageException {
        super(data);
        this.userName = data.decodeString();
        this.token = data.decodeString();
    }

    @Override
    public ByteData encode(){
        return super.encode()
                .append(userName)
                .append(token);
    }

    public String getUserName() {
        return userName;
    }

    public String getToken() {
        return token;
    }

}
