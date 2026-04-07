package persistence;

import java.util.HashMap;
import java.util.Map;

public class PersistenceContext {

    private final Map<Class<?>, Map<Object, Object>> cache = new HashMap<>();

    public <T> T get(Class<T> entityClass, Object id) {
        Map<Object, Object> entityMap = cache.get(entityClass);
        if (entityMap == null) {
            return null;
        }
        return entityClass.cast(entityMap.get(id));
    }

    public void put(Class<?> entityClass, Object id, Object entity) {
        cache.computeIfAbsent(entityClass, k -> new HashMap<>())
             .put(id, entity);
    }
}
