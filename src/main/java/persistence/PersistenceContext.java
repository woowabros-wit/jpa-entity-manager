package persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PersistenceContext {

    private final Map<EntityKey, EntityEntry> entityEntries = new HashMap<>();

    public Object get(EntityKey key) {
        Objects.requireNonNull(key, "key 는 필수 입니다.");
        final EntityEntry entityEntry = entityEntries.get(key);
        if (entityEntry == null) {
            return null;
        }
        return entityEntry.getEntity();
    }

    public void put(EntityKey entityKey, Object entity) {
        Objects.requireNonNull(entityKey, "entityKey 는 필수 입니다.");
        Objects.requireNonNull(entity, "entity 는 필수 입니다.");
        entityEntries.put(entityKey, new EntityEntry(entity));
    }

    public List<Object> getModifiedEntities() {
        return entityEntries.values().stream()
                .filter(EntityEntry::isModified)
                .map(EntityEntry::getEntity)
                .toList();
    }

}
