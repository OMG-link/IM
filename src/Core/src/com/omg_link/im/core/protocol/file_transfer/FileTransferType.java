package com.omg_link.im.core.protocol.file_transfer;

public enum FileTransferType {
    ChatFile, ChatImage, Avatar;

    public boolean canBeCached(){
        switch (this){
            case Avatar:
            case ChatImage: {
                return true;
            }
            default: {
                return false;
            }
        }
    }

}
