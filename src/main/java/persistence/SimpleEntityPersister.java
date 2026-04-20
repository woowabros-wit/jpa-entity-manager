package persistence;

import java.sql.SQLException;

public record SimpleEntityPersister(QueryExecutor queryExecutor) {

    public void insert(Object entity) throws SQLException {
        EntityMetaQuery metaQuery = new EntityMetaQuery(entity.getClass());
        queryExecutor.execute(metaQuery.buildInsert(), metaQuery.extractInsertParams(entity));
    }

    public void update(Object entity) throws SQLException {
        EntityMetaQuery metaQuery = new EntityMetaQuery(entity.getClass());
        queryExecutor.execute(metaQuery.buildUpdate(), metaQuery.extractUpdateParams(entity));
    }

    public void delete(Object entity) throws SQLException {
        EntityMetaQuery metaQuery = new EntityMetaQuery(entity.getClass());
        queryExecutor.execute(metaQuery.buildDelete(), metaQuery.extractDeleteParams(entity));
    }
}
