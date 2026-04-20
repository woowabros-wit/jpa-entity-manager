package persistence;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityMetadata {
    private static final Map<Class<?>, EntityMetadata> CACHE = new ConcurrentHashMap<>();

    private final String tableName;
    private final Field idField;
    private final String idColumnName;
    private final List<Field> fields;

    public static EntityMetadata of(Class<?> entityClass) {
        return CACHE.computeIfAbsent(entityClass, EntityMetadata::new);
    }

    private EntityMetadata(Class<?> entityClass) {
        this.tableName = extractTableName(entityClass);
        this.fields = extractFields(entityClass);
        this.idField = extractIdField(this.fields);
        this.idColumnName = getColumnName(this.idField);
    }

    public String getTableName() {
        return tableName;
    }

    public Field getIdField() {
        return idField;
    }

    public String getIdColumnName() {
        return idColumnName;
    }

    public List<Field> getFields() {
        return fields;
    }

    public String getColumnName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            return field.getAnnotation(Column.class).name();
        }
        return field.getName();
    }

    private static String extractTableName(Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(Table.class)) {
            return entityClass.getAnnotation(Table.class).name();
        }
        return entityClass.getSimpleName().toLowerCase();
    }

    private static List<Field> extractFields(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields()).toList();
    }

    private static Field extractIdField(List<Field> fields) {
        return fields.stream()
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("ID 필드가 존재하지 않습니다."));
    }
}
