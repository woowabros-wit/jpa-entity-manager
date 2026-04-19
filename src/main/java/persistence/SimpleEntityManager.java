package persistence;

import jakarta.persistence.EntityTransaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SimpleEntityManager {
    private final Connection connection;
    private final PersistenceContext persistenceContext;
    private final QueryExecutor queryExecutor;
    private final EntityTransaction transaction;
    private final List<Object> actionQueue = new ArrayList<>();

    public SimpleEntityManager(Connection connection) {
        this.connection = connection;
        this.persistenceContext = new PersistenceContext();
        this.queryExecutor = new QueryExecutor(connection);
        this.transaction = new EntityTransactionImpl(connection);
    }

    public <T> T find(Class<T> entityClass, Object id) throws Exception {
        T cached = persistenceContext.get(entityClass, id);
        if (cached != null) {
            return cached;
        }

        String sql = new EntityMetaQuery(entityClass).buildFindById();
        List<T> results = queryExecutor.query(sql, entityClass, id);
        if (results.isEmpty()) {
            return null;
        }

        T entity = results.getFirst();
        persistenceContext.put(entityClass, id, entity);
        return entity;
    }

    public void persist(Object entity) {
        actionQueue.add(entity);
    }

    public void flush() {
        try {
            for (Object entity : actionQueue) {
                EntityMetaQuery metaQuery = new EntityMetaQuery(entity.getClass());
                queryExecutor.execute(metaQuery.buildInsert(), metaQuery.extractInsertParams(entity));
            }

            actionQueue.clear();

            for (var dirty : persistenceContext.getDirtyEntities()) {
                EntityMetaQuery metaQuery = new EntityMetaQuery(dirty.entityClass());
                queryExecutor.execute(metaQuery.buildUpdate(), metaQuery.extractUpdateParams(dirty.entity()));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public EntityTransaction getTransaction() {
        return transaction;
    }

    public void close() throws SQLException {
        connection.close();
    }
}
