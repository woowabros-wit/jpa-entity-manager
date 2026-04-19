package persistence;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PersistenceContext {

    private final Map<Class<?>, EntityStore> cache = new HashMap<>();
    private final Map<Class<?>, EntityStore> snapshots = new HashMap<>();

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
        snapshots.computeIfAbsent(entityClass, k -> new EntityStore())
             .put(id, copyEntity(entity));
    }

    public List<DirtyEntity> getDirtyEntities() {
        List<DirtyEntity> dirtyEntities = new ArrayList<>();
        for (var entry : cache.entrySet()) {
            Class<?> entityClass = entry.getKey();
            EntityStore snapshotStore = snapshots.get(entityClass);
            if (snapshotStore == null) continue;

            for (var entityEntry : entry.getValue().getAll().entrySet()) {
                Object id = entityEntry.getKey();
                Object current = entityEntry.getValue();
                Object snapshot = snapshotStore.get(id);
                if (snapshot != null && isDirty(current, snapshot)) {
                    dirtyEntities.add(new DirtyEntity(entityClass, current));
                }
            }
        }
        return dirtyEntities;
    }

    private boolean isDirty(Object current, Object snapshot) {
        for (Field field : current.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                if (!Objects.equals(field.get(current), field.get(snapshot))) {
                    return true;
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private Object copyEntity(Object entity) {
        try {
            Class<?> clazz = entity.getClass();
            Object copy = clazz.getDeclaredConstructor().newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                field.set(copy, field.get(entity));
            }
            return copy;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public record DirtyEntity(Class<?> entityClass, Object entity) {}
}
