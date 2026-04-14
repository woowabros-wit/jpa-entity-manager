package persistence;

import persistence.annotation.Id;
import persistence.annotation.Table;

import java.lang.reflect.Field;

public class EntityMetaQuery {

    private final String tableName;
    private final String idColumn;

    public EntityMetaQuery(Class<?> entityClass) {
        this.tableName = resolveTableName(entityClass);
        this.idColumn = resolveIdColumn(entityClass);
    }

    public String buildFindById() {
        return new SelectQueryBuilder()
                .select("*")
                .from(tableName)
                .build() + " WHERE " + idColumn + " = ?";
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
