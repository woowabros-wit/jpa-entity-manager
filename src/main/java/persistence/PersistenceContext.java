package persistence;

import java.util.HashMap;
import java.util.Map;

public class PersistenceContext {

    private final Map<Class<?>, EntityStore> cache = new HashMap<>();

    public <T> T get(Class<T> entityClass, Object id) {
        EntityStore store = cache.get(entityClass);
        if (store == null) {
            return null;
        }
        return entityClass.cast(store.get(id));
    }

    public void put(Class<?> entityClass, Object id, Object entity) {
        cache.computeIfAbsent(entityClass, k -> new EntityStore())
             .put(id, entity);
    }
}
