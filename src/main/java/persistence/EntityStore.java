package persistence;

import java.util.HashMap;
import java.util.Map;

public class EntityStore {

    private final Map<Object, Object> entities = new HashMap<>();

    public Object get(Object id) {
        return entities.get(id);
    }

    public void put(Object id, Object entity) {
        entities.put(id, entity);
    }

    public void remove(Object id) {
        entities.remove(id);
    }

    public Map<Object, Object> getAll() {
        return entities;
    }
}
