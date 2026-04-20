package persistence;

import jdbc.GeneratedKey;
import jdbc.QueryExecutor;
import query.InsertQueryBuilder;
import query.UpdateQueryBuilder;
import util.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public class EntityPersister {

    private final QueryExecutor queryExecutor;
    private final EntityMetaDataCache entityMetaDataCache;

    public EntityPersister(Connection connection, EntityMetaDataCache entityMetaDataCache) {
        this(new QueryExecutor(connection, entityMetaDataCache), entityMetaDataCache);
    }

    public EntityPersister(QueryExecutor queryExecutor, EntityMetaDataCache entityMetaDataCache) {
        this.queryExecutor = queryExecutor;
        this.entityMetaDataCache = entityMetaDataCache;
    }

    public Object insert(Object target) {
        final Class<?> targetClass = target.getClass();
        final EntityMetaData entityMetaData = entityMetaDataCache.get(targetClass);

        final Field idField = entityMetaData.getIdField();
        final Object idValue = ReflectionUtils.getValue(idField, target);
        if (idValue == null) {
            return executeGeneratedValueInsert(target, entityMetaData);
        } else {
            return executeInsert(target, entityMetaData);
        }
    }

    private Object executeGeneratedValueInsert(Object target, EntityMetaData entityMetaData) {
        final List<String> columnNamesExcludeIdColumn = entityMetaData.getColumnNamesExcludeIdColumn();
        final String sql = createInsertSql(entityMetaData.getTableName(), columnNamesExcludeIdColumn);
        final Object[] columnValues = getColumnValues(target, columnNamesExcludeIdColumn, entityMetaData);
        final GeneratedKey keyHolder = new GeneratedKey(entityMetaData.getIdColumnName());
        queryExecutor.execute(sql, keyHolder, columnValues);
        final Field idField = entityMetaData.getIdField();
        ReflectionUtils.setValue(idField, target, keyHolder.getKey());
        return target;
    }

    private Object executeInsert(Object target, EntityMetaData entityMetaData) {
        final List<String> allColumnNames = entityMetaData.getAllColumnNames();
        final String sql = createInsertSql(entityMetaData.getTableName(), allColumnNames);
        final Object[] columnValues = getColumnValues(target, allColumnNames, entityMetaData);
        queryExecutor.execute(sql, columnValues);
        return target;
    }

    private String createInsertSql(String tableName, List<String> columnNames) {
        final InsertQueryBuilder builder = new InsertQueryBuilder()
                .into(tableName);

        for (String columnName : columnNames) {
            builder.value(columnName, "?");
        }
        return builder.build();
    }

    public void update(Object entity) {
        final EntityMetaData entityMetaData = entityMetaDataCache.get(entity);
        final List<String> columnNamesExcludeIdColumn = entityMetaData.getColumnNamesExcludeIdColumn();
        final String sql = createUpdateSql(entityMetaData, columnNamesExcludeIdColumn);
        final Object[] params = extractUpdateParameters(entity, columnNamesExcludeIdColumn, entityMetaData);
        final int update = queryExecutor.execute(sql, params);
        if (update == 0) {
            throw new RuntimeException("업데이트에 실패 했습니다. entity: [%s]".formatted(entity.getClass()));
        }
    }

    private Object[] extractUpdateParameters(Object entity, List<String> columnNamesExcludeIdColumn, EntityMetaData entityMetaData) {
        final Object[] columnValues = getColumnValues(entity, columnNamesExcludeIdColumn, entityMetaData);

        final Object[] results = new Object[columnValues.length + 1];
        System.arraycopy(columnValues, 0, results, 0, columnValues.length);

        final Object idValue = ReflectionUtils.getValue(entityMetaData.getIdField(), entity);
        results[results.length - 1] = idValue;
        return results;
    }

    private String createUpdateSql(EntityMetaData entityMetaData, List<String> columnNamesExcludeIdColumn) {
        final String tableName = entityMetaData.getTableName();
        final String idColumnName = entityMetaData.getIdColumnName();
        final UpdateQueryBuilder builder = new UpdateQueryBuilder().table(tableName);
        for (String columnName : columnNamesExcludeIdColumn) {
            builder.set(columnName, "?");
        }
        builder.where(idColumnName + " = ?");
        return builder.build();
    }

    private Object [] getColumnValues(Object target, List<String> columnNamesExcludeIdColumn, EntityMetaData entityMetaData) {
        return columnNamesExcludeIdColumn.stream()
                .map(entityMetaData::getField)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(field -> ReflectionUtils.getValue(field, target))
                .toArray(Object[]::new);
    }

}
