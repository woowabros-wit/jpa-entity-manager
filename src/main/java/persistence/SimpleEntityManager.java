package persistence;


import entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SimpleEntityManager {
    private final Connection connection;
    private final QueryExecutor executor;
    private PersistenceContext persistenceContext;

    public SimpleEntityManager(Connection connection) {
        this.connection = connection;
        executor = new QueryExecutor(connection);
        persistenceContext = new PersistenceContext();
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


        String sql = new QueryExtractor()
                .getSelectQuery(entityClass, key);

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

    public void persist(User user) {

    }

    public void flush() {

    }
}
