package persistence;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityMetadata {
    private final String tableName;
    private final String idColumnName;

    private static final Map<Class<?>, EntityMetadata> CACHE = new ConcurrentHashMap<>();

    public static EntityMetadata of(Class<?> entityClass) {
        return CACHE.computeIfAbsent(entityClass, EntityMetadata::new);
    }

    private EntityMetadata(Class<?> entityClass) {
        this.tableName = extractTableName(entityClass);

        Field idField = extractIdField(entityClass);
        this.idColumnName = extractColumnName(idField);
    }

    private String extractColumnName(Field field) {
        return field.getName();
    }

    private Field extractIdField(Class<?> entityClass) {
        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }

        throw new IllegalArgumentException("ID 필드가 존재하지 않습니다.");
    }

    private String extractTableName(Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(Table.class)) {
            return entityClass.getAnnotation(Table.class).name();
        }
        return entityClass.getSimpleName().toLowerCase();
    }

    public String getTableName() {
        return tableName;
    }

    public String getIdColumnName() {
        return idColumnName;
    }
}
