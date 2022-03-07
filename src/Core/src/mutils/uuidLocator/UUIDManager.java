package mutils.uuidLocator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UUIDManager {
    public Map<UUID, IUuidLocatable> map = new HashMap<>();

    public void insert(IUuidLocatable uuidLocatable) throws UuidConflictException {
        if(map.containsKey(uuidLocatable.getUuid())){
            throw new UuidConflictException();
        }
        map.put(uuidLocatable.getUuid(),uuidLocatable);
    }

    public Object get(UUID uuid){
        return map.get(uuid);
    }

    public void remove(UUID uuid){
        map.remove(uuid);
    }

    public void remove(IUuidLocatable uuidLocatable){
        remove(uuidLocatable.getUuid());
    }

}