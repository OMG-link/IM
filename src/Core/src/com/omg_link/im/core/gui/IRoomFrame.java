package com.omg_link.im.core.gui;

import com.omg_link.im.core.protocol.data_pack.chat.ChatFileBroadcastPack;
import com.omg_link.im.core.protocol.data_pack.chat.ChatImageBroadcastPack;
import com.omg_link.im.core.protocol.data_pack.chat.ChatTextBroadcastPack;
import com.omg_link.im.core.user_manager.User;
import com.omg_link.utils.IStringGetter;

import java.util.Collection;

public interface IRoomFrame {

    enum ExitReason{
        Unknown, InvalidUrl, InvalidToken, PackageDecodeError, ClientException, ConnectingToNewRoom
    }

    void exitRoom(ExitReason reason);

    /**
     * Called when connection to the server has been built.
     * This function should allow user to type texts and so on.
     */
    void onConnectionBuilt();
    void onConnectionBroke();

    void showSystemMessage(String message);
    void showTextMessage(ChatTextBroadcastPack pack, boolean isSelfSent);
    IFileTransferringPanel showFileUploadedMessage(ChatFileBroadcastPack pack, boolean isSelfSent);
    IFileTransferringPanel showChatImageMessage(ChatImageBroadcastPack pack, boolean isSelfSent);

    void onRoomNameUpdate(String roomName);
    IFileTransferringPanel addFileTransferringPanel(IStringGetter fileNameGetter,long fileSize);

    void updateUserList(Collection<User> userList);
    void onUserJoined(User user);
    void onUserLeft(User user);
    void onUsernameChanged(User user,String previousName);

}
