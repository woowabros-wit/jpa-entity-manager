package persistence;

import persistence.annotation.Id;
import persistence.annotation.Table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class EntityMetadata {

    private static final Map<Class<?>, EntityMetadata> CACHE = new ConcurrentHashMap<>();

    private final Class<?> entityClass;
    private final String tableName;
    private final Field idField;
    private final List<Field> columnFields;

    private EntityMetadata(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.tableName = resolveTableName();
        this.idField = resolveIdField();
        this.columnFields = resolveColumnFields();
    }

    public static EntityMetadata of(Class<?> entityClass) {
        return CACHE.computeIfAbsent(entityClass, EntityMetadata::new);
    }

    public String getTableName() {
        return tableName;
    }

    public String getIdColumnName() {
        return idField.getName();
    }

    public List<Field> getColumnFields() {
        return columnFields;
    }

    public Object getIdValue(Object entity) {
        try {
            return idField.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Object[] getColumnValues(Object entity) {
        return columnFields.stream()
                .map(field -> {
                    try {
                        return field.get(entity);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray();
    }

    public boolean isDirty(Object current, Object snapshot) {
        for (Field field : columnFields) {
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

    public Object copyEntity(Object entity) {
        try {
            Object copy = entityClass.getDeclaredConstructor().newInstance();
            for (Field field : entityClass.getDeclaredFields()) {
                field.setAccessible(true);
                field.set(copy, field.get(entity));
            }
            return copy;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String resolveTableName() {
        Table table = entityClass.getAnnotation(Table.class);
        if (table != null && !table.name().isEmpty()) {
            return table.name();
        }
        throw new IllegalStateException("Table Annotation 없음");
    }

    private Field resolveIdField() {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalStateException("Id Annotation 없음");
    }

    private List<Field> resolveColumnFields() {
        List<Field> fields = new ArrayList<>();
        for (Field field : entityClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                fields.add(field);
            }
        }
        return fields;
    }
}
