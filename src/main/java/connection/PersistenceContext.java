package connection;

import java.util.HashMap;
import java.util.Map;

public class PersistenceContext {

    private final Map<Long, Object> firstCache = new HashMap<>();

    public void put(Long id, Object entity) {
        firstCache.put(id, entity);
    }

    public <T> T get(Long id) {
        return (T) firstCache.get(id);
    }

    public boolean hasId(Long id) {
        return firstCache.containsKey(id);
    }
}
