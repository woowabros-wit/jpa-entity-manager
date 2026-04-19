package persistence;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EntityMetaQuery {

    private final EntityMetadata metadata;

    public EntityMetaQuery(Class<?> entityClass) {
        this.metadata = EntityMetadata.of(entityClass);
    }

    public String buildFindById() {
        return new SelectQueryBuilder()
                .select("*")
                .from(metadata.getTableName())
                .build() + " WHERE " + metadata.getIdColumnName() + " = ?";
    }

    public String buildInsert() {
        InsertQueryBuilder builder = new InsertQueryBuilder().into(metadata.getTableName());
        for (Field field : metadata.getColumnFields()) {
            builder.value(field.getName(), "?");
        }
        return builder.build();
    }

    public Object[] extractInsertParams(Object entity) {
        return metadata.getColumnValues(entity);
    }

    public String buildUpdate() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder().table(metadata.getTableName());
        for (Field field : metadata.getColumnFields()) {
            builder.set(field.getName(), "?");
        }
        builder.where(metadata.getIdColumnName() + " = ?");
        return builder.build();
    }

    public Object[] extractUpdateParams(Object entity) {
        List<Object> params = new ArrayList<>(List.of(metadata.getColumnValues(entity)));
        params.add(metadata.getIdValue(entity));
        return params.toArray();
    }

    public Object extractIdValue(Object entity) {
        return metadata.getIdValue(entity);
    }
}
