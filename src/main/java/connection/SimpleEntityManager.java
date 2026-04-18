package connection;

import jdbc.NamedParameterQuery;
import jdbc.QueryExecutor;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
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

    public Connection getConnection() {
        return connection;
    }

    public void close() throws SQLException {
        this.connection.close();
    }

    public <T> T find(Class<T> clazz, Long id) throws Exception {
        if (this.persistenceContext.hasId(id)) {
            Object result = this.persistenceContext.get(id);
            if (clazz.isInstance(result)) {
                return (T) result;
            }
        }

        String tableName = getTableName(clazz);
        String idName = getIdName(clazz);

        String sql = "SELECT * FROM " + tableName + " WHERE " + idName + " = :id";

        NamedParameterQuery namedParameterQuery = new NamedParameterQuery(sql);
        namedParameterQuery.setParameter("id", id);
        T result = queryExecutor.query(namedParameterQuery, clazz).getFirst();
        this.persistenceContext.put(id, result);
        return result;
    }

    private <T> String getTableName(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("table 어노테이션이 없음");
        }
        return clazz.getAnnotation(Table.class).name();
    }

    private <T> String getIdName(Class<T> clazz) {
        List<Field> fields = Arrays.stream(clazz.getDeclaredFields())
                .filter(it -> it.getAnnotation(Id.class) != null)
                .toList();

        if (fields.size() != 1) {
            throw new IllegalArgumentException("id 어노테이션이 붙은 필드는 무조건 한개여야 함");
        }
        return fields.getFirst().getName();
    }
}
