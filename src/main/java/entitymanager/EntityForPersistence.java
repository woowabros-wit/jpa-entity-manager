package entitymanager;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

class EntityForPersistence {

    private Object entity;
    private Status status;
    private Map<String, Object> snapshot;

    public EntityForPersistence(Object entity, Status status) {
        this.entity = entity;
        this.status = status;
        this.snapshot = takeSnapshot(entity);
    }

    public static EntityForPersistence createOf(Object entity) {
        return new EntityForPersistence(entity, Status.CREATED);
    }

    public static EntityForPersistence updateOf(Object entity) {
        return new EntityForPersistence(entity, Status.UPDATED);
    }

    public static EntityForPersistence deleteOf(Object entity) {
        return new EntityForPersistence(entity, Status.DELETED);
    }

    public boolean isDeleted() {
        return status == Status.DELETED;
    }

    public boolean isDirty() {
        Map<String, Object> current = takeSnapshot(entity);
        return !current.equals(snapshot);
    }

    public Status getStatus() {
        return status;
    }

    public Object getEntity() {
        return entity;
    }

    private static Map<String, Object> takeSnapshot(Object entity) {
        Map<String, Object> snap = new LinkedHashMap<>();
        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                snap.put(field.getName(), field.get(entity));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("스냅샷 생성에 실패했습니다.", e);
            } finally {
                field.setAccessible(false);
            }
        }
        return snap;
    }

    enum Status {
        CREATED, UPDATED, DELETED
    }
}
