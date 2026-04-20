package entitymanager;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PersistenceContext {
    private final Map<Class<?>, Map<Object, Object>> cache = new HashMap<>();
    private final Map<Class<?>, Map<Object, Map<String, Object>>> snapshots = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> entityClass, Object id) {
        Map<Object, Object> entityMap = cache.get(entityClass);
        if (entityMap == null) {
            return null;
        }
        return (T) entityMap.get(id);
    }

    public void put(Class<?> entityClass, Object id, Object entity) {
        cache.computeIfAbsent(entityClass, k -> new HashMap<>()).put(id, entity);
        takeSnapshot(entityClass, id, entity);
    }

    public Map<Class<?>, Map<Object, Object>> getManagedEntities() {
        return Collections.unmodifiableMap(cache);
    }

    public Map<String, Object> getSnapshot(Class<?> entityClass, Object id) {
        Map<Object, Map<String, Object>> entitySnapshots = snapshots.get(entityClass);
        if (entitySnapshots == null) {
            return null;
        }
        return entitySnapshots.get(id);
    }

    private void takeSnapshot(Class<?> entityClass, Object id, Object entity) {
        Map<String, Object> snapshot = new HashMap<>();
        for (Field field : entityClass.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                snapshot.put(field.getName(), field.get(entity));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        snapshots.computeIfAbsent(entityClass, k -> new HashMap<>()).put(id, snapshot);
    }
}
