package persistence;


import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SimpleEntityManager {
    private final Connection connection;
    private final QueryExecutor executor;

    public SimpleEntityManager(Connection connection) {
        this.connection = connection;
        executor = new QueryExecutor(connection);
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() throws SQLException {
        connection.close();
    }

    public PersistenceContext getPersistenceContext() {
        return new PersistenceContext();
    }

    public Transaction getTransaction() {
        return new Transaction(connection);
    }

    public <T, ID> T find(Class<T> entityClass, ID id) throws Exception {
        String table = getTable(entityClass);
        String idField = getId(entityClass);

        String sql = new SelectQueryBuilder()
                .select()
                .from(table)
                .where(String.format("%s = ?", idField))
                .build();


        List<T> result = executor.query(sql, entityClass, id);
        if (result == null || result.isEmpty()) {
            return null;
        }

        if (result.size() > 1) {
            throw new IllegalStateException("동일한 ID를 가진 엔티티가 여러 개 존재합니다.");
        }

        return (T) result.get(0);
    }

    private <T> String getId(Class<T> entityClass) {
        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                return field.getName();
            }
        }

        throw new IllegalArgumentException("ID 필드가 존재하지 않습니다.");
    }

    private <T> String getTable(Class<T> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table != null) {
            return table.name();
        }

        return entityClass.getSimpleName().toLowerCase() + "s";
    }
}
