package persistence;

import java.util.LinkedHashMap;
import java.util.Map;

public class PersistenceCacheManager {
    private final Map<EntityKey, Object> entityMap;

    public PersistenceCacheManager() {
        this.entityMap = new LinkedHashMap<>();
    }

    public <T> T get(Class<T> entityClass, Object key) {
        EntityKey entityKey = new EntityKey(entityClass, key);
        Object entity = entityMap.get(entityKey);
        return entity != null ? (T) entity : null;
    }

    public <T> void save(Class<T> entityClass, Object key, T entity) {
        EntityKey entityKey = new EntityKey(entityClass, key);
        entityMap.put(entityKey, entity);
    }
}
