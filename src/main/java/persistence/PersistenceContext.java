package persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        EntityMetadata metadata = EntityMetadata.of(entityClass);
        cache.computeIfAbsent(entityClass, k -> new EntityStore())
             .put(id, entity);
        snapshots.computeIfAbsent(entityClass, k -> new EntityStore())
             .put(id, metadata.copyEntity(entity));
    }

    public void remove(Class<?> entityClass, Object id) {
        EntityStore store = cache.get(entityClass);
        if (store != null) {
            store.remove(id);
        }
        EntityStore snapshotStore = snapshots.get(entityClass);
        if (snapshotStore != null) {
            snapshotStore.remove(id);
        }
    }

    public List<DirtyEntity> getDirtyEntities() {
        List<DirtyEntity> dirtyEntities = new ArrayList<>();
        for (var entry : cache.entrySet()) {
            Class<?> entityClass = entry.getKey();
            EntityMetadata metadata = EntityMetadata.of(entityClass);
            EntityStore snapshotStore = snapshots.get(entityClass);
            if (snapshotStore == null) continue;

            for (var entityEntry : entry.getValue().getAll().entrySet()) {
                Object id = entityEntry.getKey();
                Object current = entityEntry.getValue();
                Object snapshot = snapshotStore.get(id);
                if (snapshot != null && metadata.isDirty(current, snapshot)) {
                    dirtyEntities.add(new DirtyEntity(entityClass, current));
                    snapshotStore.put(id, metadata.copyEntity(current));
                }
            }
        }
        return dirtyEntities;
    }

    public record DirtyEntity(Class<?> entityClass, Object entity) {}
}
