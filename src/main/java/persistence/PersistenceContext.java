package persistence;

import java.util.LinkedHashMap;
import java.util.Map;

public class PersistenceContext {
    private final Map<Class<?>, Map<Object, Object>> cache;

    public PersistenceContext() {
        this.cache = new LinkedHashMap<>();
    }

    public <T> T get(Class<T> entityClass, Object key) {
        return cache.get(entityClass) != null ? (T) cache.get(entityClass).get(key) : null;
    }

    public <T> void save(Class<T> entityClass, Object key, T entity) {
        Map<Object, Object> idObjectMap = cache.get(entityClass);
        if (idObjectMap == null) {
            idObjectMap = new LinkedHashMap<>();
            cache.put(entityClass, idObjectMap);
        }
        idObjectMap.put(key, entity);
    }
}
