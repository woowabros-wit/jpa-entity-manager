package entitymanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersistenceContext {
    private final Map<Class<?>, Map<Object, Object>> cache = new HashMap<>();
    private final List<Object> newEntities = new ArrayList<>();

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
    }

    public void addNewEntity(Object entity) {
        newEntities.add(entity);
    }

    public List<Object> getNewEntities() {
        return Collections.unmodifiableList(newEntities);
    }

    public void clearNewEntities() {
        newEntities.clear();
    }
}
