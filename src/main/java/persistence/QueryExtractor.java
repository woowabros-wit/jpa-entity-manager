package persistence;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class QueryExtractor<T> {

    private final Class<T> entityClass;
    private final EntityMetadata metadata;

    public QueryExtractor(final Class<T> entityClass) {
        this.entityClass = entityClass;
        this.metadata = EntityMetadata.of(entityClass);
    }

    public String getSelectQuery() {
        return new SelectQueryBuilder()
                .select()
                .from(metadata.getTableName())
                .where(String.format("%s = ?", metadata.getIdColumnName()))
                .build();
    }

    public String getInsertQuery(Object entity) throws Exception {
        InsertQueryBuilder builder = new InsertQueryBuilder().into(metadata.getTableName());
        for (Field field : insertableFields(entity)) {
            builder.value(field.getName(), "?");
        }
        return builder.build();
    }

    public Object[] getInsertParams(Object entity) throws Exception {
        List<Object> params = new ArrayList<>();
        for (Field field : insertableFields(entity)) {
            params.add(field.get(entity));
        }
        return params.toArray();
    }

    private List<Field> insertableFields(Object entity) throws IllegalAccessException {
        List<Field> fields = new ArrayList<>();
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                continue;
            }
            field.setAccessible(true);
            if (field.get(entity) == null) {
                continue;
            }
            fields.add(field);
        }
        return fields;
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
}
