package persistence;

import java.util.Objects;

public class EntityKey {

    private final Class<?> entityClass;
    private final Object id;

    public EntityKey(Class<?> entityClass, Object id) {
        this.entityClass = Objects.requireNonNull(entityClass, "entityClass 는 필수 입니다.");
        this.id = Objects.requireNonNull(id, "id 는 필수 입니다.");
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof EntityKey entityKey)) return false;

        return entityClass.equals(entityKey.entityClass) && id.equals(entityKey.id);
    }

    @Override
    public int hashCode() {
        int result = entityClass.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

}
