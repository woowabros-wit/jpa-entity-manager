package persistence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SimpleEntityManager {
    private final Connection connection;
    private final PersistenceContext persistenceContext;
    private final QueryExecutor queryExecutor;

    public SimpleEntityManager(Connection connection) {
        this.connection = connection;
        this.persistenceContext = new PersistenceContext();
        this.queryExecutor = new QueryExecutor(connection);
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

    public Connection getConnection() {
        return connection;
    }

    public void close() throws SQLException {
        connection.close();
    }
}
