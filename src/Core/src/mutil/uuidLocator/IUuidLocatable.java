package mutil.uuidLocator;

import java.util.UUID;

public interface IUuidLocatable {
    //为此内容指定UUID
    UUID getUuid();

    //为此内容指定UUID管理器
    UUIDManager getUuidManager();

    //调用此函数以将内容加入UUID管理器
    default void onCreate() throws UuidConflictException {
        getUuidManager().insert(this);
    }

    //调用此函数以将内容移出UUID管理器
    default void onDelete(){
        getUuidManager().remove(this);
    }

}
