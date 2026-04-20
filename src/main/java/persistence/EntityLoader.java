package persistence;

import jdbc.QueryExecutor;
import query.SelectQueryBuilder;

import java.sql.Connection;

public class EntityLoader {

    private final QueryExecutor queryExecutor;
    private final EntityMetaDataCache entityMetaDataCache;

    public EntityLoader(Connection connection, EntityMetaDataCache entityMetaDataCache) {
        this(new QueryExecutor(connection, entityMetaDataCache), entityMetaDataCache);
    }

    public EntityLoader(QueryExecutor queryExecutor, EntityMetaDataCache entityMetaDataCache) {
        this.queryExecutor = queryExecutor;
        this.entityMetaDataCache = entityMetaDataCache;
    }

    public <T> T load(Class<T> entityClass, Object id) {
        final EntityMetaData entityMetaData = entityMetaDataCache.get(entityClass);
        final String tableName = entityMetaData.getTableName();
        final String idColumnName = entityMetaData.getIdColumnName();

        final String sql = new SelectQueryBuilder()
                .from(tableName)
                .where(idColumnName + " = ?")
                .build();

        return queryExecutor.queryForObject(sql, entityClass, id);
    }

}
