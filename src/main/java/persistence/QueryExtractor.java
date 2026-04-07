package persistence;

import java.lang.reflect.Field;

public class QueryExtractor {
    public <T> String getSelectQuery(Class<T> entityClass, Object key) {
        String table = getTable(entityClass);
        String idField = getId(entityClass);

        String sql = new SelectQueryBuilder()
                .select()
                .from(table)
                .where(String.format("%s = ?", idField))
                .build();

        return sql;
    }

    private <T> String getId(Class<T> entityClass) {
        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                return field.getName();
            }
        }

        throw new IllegalArgumentException("ID 필드가 존재하지 않습니다.");
    }

    private <T> String getTable(Class<T> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table != null) {
            return table.name();
        }

        return entityClass.getSimpleName().toLowerCase() + "s";
    }
}
