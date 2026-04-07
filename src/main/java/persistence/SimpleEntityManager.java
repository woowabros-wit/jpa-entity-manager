package persistence;

import annotation.Id;
import annotation.Table;

import java.lang.reflect.Field;
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

        String tableName = resolveTableNameAnnotation(entityClass);
        String idColumn = resolveIdColumnName(entityClass);

        String sql = new SelectQueryBuilder()
                .select("*")
                .from(tableName)
                .build() + " WHERE " + idColumn + " = ?";

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

    private String resolveTableNameAnnotation(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table != null && !table.name().isEmpty()) {
            return table.name();
        }
        throw new IllegalStateException("Table Annotation 없음");
    }

    private String resolveIdColumnName(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field.getName();
            }
        }
        throw new IllegalStateException("Id Annotation 없음");
    }
}
