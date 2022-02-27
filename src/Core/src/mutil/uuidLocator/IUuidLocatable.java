package mutil.uuidLocator;

import java.util.UUID;

public interface IUuidLocatable {
    UUID getUuid();

    UUIDManager getUuidManager();

    default void onCreate(){
        getUuidManager().insert(this);
    }

    default void onDelete(){
        getUuidManager().remove(this);
    }

}
