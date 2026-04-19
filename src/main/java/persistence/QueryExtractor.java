package persistence;

import java.lang.reflect.Field;

public class QueryExtractor<T> {

    private final String tableName;
    private final String idFieldName;

    public QueryExtractor(final Class<T> enttiyClass) {
        this.tableName = getTable(enttiyClass);
        this.idFieldName = getId(enttiyClass);
    }

    public String getSelectQuery() {
        String sql = new SelectQueryBuilder()
                .select()
                .from(tableName)
                .where(String.format("%s = ?", idFieldName))
                .build();

        return sql;
    }

    private String getId(Class<T> entityClass) {
        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                return field.getName();
            }
        }

        throw new IllegalArgumentException("ID 필드가 존재하지 않습니다.");
    }

    private String getTable(Class<T> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table != null) {
            return table.name();
        }

        return entityClass.getSimpleName().toLowerCase() + "s";
    }
}
