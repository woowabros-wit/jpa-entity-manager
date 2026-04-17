package entitymanager;

import static entitymanager.EntityUtils.extractIdFieldValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PersistenceContext {

    private Map<Class<?>, Map<Long, EntityForPersistence>> entities = new HashMap<>();

    public <T> void save(Class<T> clazz, EntityForPersistence entity) {
        if (entity == null) {
            return;
        }

        Map<Long, EntityForPersistence> entitiesByClazz = entities.getOrDefault(clazz, new HashMap<>());
        entitiesByClazz.put(extractIdFieldValue(entity.getEntity()), entity);

        entities.put(clazz, entitiesByClazz);
    }

    public <T> T find(Class<T> clazz, long id) {
        EntityForPersistence result = entities.getOrDefault(clazz, Collections.emptyMap()).getOrDefault(id, null);
        if (result == null || result.isDeleted()) {
            return null;
        }
        return (T) result.getEntity();
    }

    public Map<Class<?>, Map<Long, EntityForPersistence>> getAllEntities() {
        return entities;
    }

    public void remove(Class<?> clazz, Long id) {
        Map<Long, EntityForPersistence> map = entities.get(clazz);
        if (map != null) {
            map.remove(id);
        }
    }

    public long maxAssignedId(Class<?> clazz) {
        Map<Long, EntityForPersistence> map = entities.get(clazz);
        if (map == null || map.isEmpty()) {
            return 0L;
        }
        return map.keySet().stream()
            .filter(Objects::nonNull)
            .mapToLong(Long::longValue)
            .max()
            .orElse(0L);
    }
}
