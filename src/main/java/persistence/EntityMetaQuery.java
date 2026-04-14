package persistence;

import persistence.annotation.Id;
import persistence.annotation.Table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EntityMetaQuery {

    private final Class<?> entityClass;
    private final String tableName;
    private final String idColumn;

    public EntityMetaQuery(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.tableName = resolveTableName(entityClass);
        this.idColumn = resolveIdColumn(entityClass);
    }

    public String buildFindById() {
        return new SelectQueryBuilder()
                .select("*")
                .from(tableName)
                .build() + " WHERE " + idColumn + " = ?";
    }

    public String buildInsert() {
        InsertQueryBuilder builder = new InsertQueryBuilder().into(tableName);
        for (Field field : entityClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Id.class)) {
                builder.value(field.getName(), "?");
            }
        }
        return builder.build();
    }

    public Object[] extractInsertParams(Object entity) {
        List<Object> params = new ArrayList<>();
        for (Field field : entityClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                try {
                    params.add(field.get(entity));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return params.toArray();
    }

    private String resolveTableName(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table != null && !table.name().isEmpty()) {
            return table.name();
        }
        throw new IllegalStateException("Table Annotation 없음");
    }

    private String resolveIdColumn(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field.getName();
            }
        }
        throw new IllegalStateException("Id Annotation 없음");
    }
}
