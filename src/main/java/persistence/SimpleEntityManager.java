package persistence;

import jakarta.persistence.EntityTransaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SimpleEntityManager {
    private final Connection connection;
    private final PersistenceContext persistenceContext;
    private final QueryExecutor queryExecutor;
    private final SimpleEntityPersister persister;
    private final EntityTransaction transaction;
    private final ActionQueue actionQueue = new ActionQueue();

    public SimpleEntityManager(Connection connection) {
        this.connection = connection;
        this.persistenceContext = new PersistenceContext();
        this.queryExecutor = new QueryExecutor(connection);
        this.persister = new SimpleEntityPersister(queryExecutor);
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
        checkDirtyEntities();
        actionQueue.addInsertAction(entity);
    }

    public void remove(Object entity) {
        checkDirtyEntities();
        EntityMetadata metadata = EntityMetadata.of(entity.getClass());
        persistenceContext.remove(entity.getClass(), metadata.getIdValue(entity));
        actionQueue.addDeleteAction(entity);
    }

    public void flush() {
        checkDirtyEntities();
        try {
            actionQueue.executeActions(persister);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public QueryExecutor getQueryExecutor() {
        return queryExecutor;
    }

    public EntityTransaction getTransaction() {
        return transaction;
    }

    public void close() throws SQLException {
        connection.close();
    }

    private void checkDirtyEntities() {
        for (var dirty : persistenceContext.getDirtyEntities()) {
            actionQueue.addUpdateAction(dirty.entity());
        }
    }
}
