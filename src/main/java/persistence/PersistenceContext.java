package persistence;

import java.util.LinkedHashMap;
import java.util.Map;

public class PersistenceContext {

    private final PersistenceCacheManager cacheManager;
    private final Map<Class<?>, Map<Object, Object>> cache;

    public PersistenceContext() {
        this.cache = new LinkedHashMap<>();
        this.cacheManager = new PersistenceCacheManager();
    }

    public <T> T get(Class<T> entityClass, Object key) {
        return cacheManager.get(entityClass, key);

    }

    public <T> void save(Class<T> entityClass, Object key, T entity) {
        cacheManager.save(entityClass, key, entity);
    }
}
