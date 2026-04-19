package persistence;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SimpleEntityManager {
    private final Connection connection;
    private final QueryExecutor executor;
    private final PersistenceContext persistenceContext;
    private final List<Object> persistQueue;

    public SimpleEntityManager(Connection connection) {
        this.connection = connection;
        this.executor = new QueryExecutor(connection);
        this.persistenceContext = new PersistenceContext();
        this.persistQueue = new ArrayList<>();
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() throws SQLException {
        connection.close();
    }

    public PersistenceContext getPersistenceContext() {
        return persistenceContext;
    }

    public Transaction getTransaction() {
        return new Transaction(connection);
    }

    public <T> T find(Class<T> entityClass, Object key) throws Exception {
        T cached = persistenceContext.get(entityClass, key);
        if (cached != null) {
            return cached;
        }

        String sql = new QueryExtractor(entityClass)
                .getSelectQuery();

        List<T> result = executor.query(sql, entityClass, key);
        if (result == null || result.isEmpty()) {
            return null;
        }

        if (result.size() > 1) {
            throw new IllegalStateException("동일한 ID를 가진 엔티티가 여러 개 존재합니다.");
        }

        T entity = result.get(0);
        persistenceContext.save(entityClass, key, entity);
        return entity;
    }

    public void persist(Object entity) {
        persistQueue.add(entity);
    }

    public void flush() {
        for (Object entity : persistQueue) {
            insert(entity);
        }
        persistQueue.clear();
    }

    private void insert(Object entity) {
        try {
            QueryExtractor extractor = new QueryExtractor(entity.getClass());
            String sql = extractor.getInsertQuery(entity);
            Object[] params = extractor.getInsertParams(entity);
            executor.execute(sql, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
