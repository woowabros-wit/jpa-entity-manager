package entitymanager;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import persistence.query.ResultSetMapper;
import persistence.query.SelectQueryBuilder;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SimpleEntityManager {
    private final Connection connection;
    private final PersistenceContext persistenceContext;
    private final ResultSetMapper resultSetMapper;

    public SimpleEntityManager(Connection connection) {
        this.connection = connection;
        this.persistenceContext = new PersistenceContext();
        this.resultSetMapper = new ResultSetMapper();
    }

    public <T> T find(Class<T> entityClass, Object id) throws Exception {
        T cached = persistenceContext.get(entityClass, id);
        if (cached != null) {
            return cached;
        }

        String tableName = resolveTableName(entityClass);
        String idColumnName = resolveIdColumnName(entityClass);

        String sql = new SelectQueryBuilder()
                .from(tableName)
                .where(idColumnName + " = ?")
                .build();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                T entity = resultSetMapper.mapToObject(rs, entityClass);
                persistenceContext.put(entityClass, id, entity);
                return entity;
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection.isClosed()) {
                return;
            }
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String resolveTableName(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        return table.name();
    }

    private String resolveIdColumnName(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field.getName();
            }
        }
        throw new IllegalStateException("@Id 필드를 찾을 수 없습니다: " + entityClass.getName());
    }
}
