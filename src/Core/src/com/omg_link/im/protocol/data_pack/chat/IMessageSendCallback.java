package com.omg_link.im.protocol.data_pack.chat;

public interface IMessageSendCallback {
    enum Reason{}

    void onSendSuccess(long serialId, long stamp);
    void onSendFailed(Reason reason);

}
