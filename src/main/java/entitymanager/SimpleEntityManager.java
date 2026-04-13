package entitymanager;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import persistence.query.InsertQueryBuilder;
import persistence.query.ResultSetMapper;
import persistence.query.SelectQueryBuilder;
import persistence.query.UpdateQueryBuilder;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SimpleEntityManager {
    private final Connection connection;
    private final PersistenceContext persistenceContext;
    private final EntityTransaction transaction;
    private final ResultSetMapper resultSetMapper;

    public SimpleEntityManager(Connection connection) {
        this.connection = connection;
        this.persistenceContext = new PersistenceContext();
        this.transaction = new EntityTransaction(connection);
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

    public void persist(Object entity) {
        persistenceContext.addNewEntity(entity);
    }

    public void flush() throws Exception {
        flushInserts();
        flushDirtyChecking();
    }

    public EntityTransaction getTransaction() {
        return transaction;
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

    private void flushInserts() throws Exception {
        List<Object> newEntities = persistenceContext.getNewEntities();
        if (newEntities.isEmpty()) {
            return;
        }

        long nextId = -1;
        String lastTableName = null;

        for (Object entity : newEntities) {
            Class<?> entityClass = entity.getClass();
            String tableName = resolveTableName(entityClass);
            Field idField = resolveIdField(entityClass);
            idField.setAccessible(true);

            if (idField.get(entity) == null) {
                if (!tableName.equals(lastTableName)) {
                    nextId = getNextId(tableName);
                    lastTableName = tableName;
                }
                idField.set(entity, nextId++);
            }

            executeInsert(entity, entityClass, tableName);
        }

        persistenceContext.clearNewEntities();
    }

    private void flushDirtyChecking() throws Exception {
        Map<Class<?>, Map<Object, Object>> managedEntities = persistenceContext.getManagedEntities();

        for (Map.Entry<Class<?>, Map<Object, Object>> classEntry : managedEntities.entrySet()) {
            Class<?> entityClass = classEntry.getKey();

            for (Map.Entry<Object, Object> entityEntry : classEntry.getValue().entrySet()) {
                Object id = entityEntry.getKey();
                Object entity = entityEntry.getValue();

                Map<String, Object> snapshot = persistenceContext.getSnapshot(entityClass, id);
                if (snapshot == null) {
                    continue;
                }

                List<String> dirtyFields = findDirtyFields(entity, entityClass, snapshot);
                if (!dirtyFields.isEmpty()) {
                    executeUpdate(entity, entityClass, id, dirtyFields);
                }
            }
        }
    }

    private List<String> findDirtyFields(Object entity, Class<?> entityClass, Map<String, Object> snapshot) throws IllegalAccessException {
        List<String> dirtyFields = new ArrayList<>();

        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                continue;
            }
            field.setAccessible(true);
            Object currentValue = field.get(entity);
            Object snapshotValue = snapshot.get(field.getName());

            if (!Objects.equals(currentValue, snapshotValue)) {
                dirtyFields.add(field.getName());
            }
        }

        return dirtyFields;
    }

    private void executeUpdate(Object entity, Class<?> entityClass, Object id, List<String> dirtyFields) throws Exception {
        String tableName = resolveTableName(entityClass);
        String idColumnName = resolveIdColumnName(entityClass);

        UpdateQueryBuilder builder = new UpdateQueryBuilder()
                .table(tableName)
                .where(idColumnName + " = ?");

        List<Object> params = new ArrayList<>();
        for (String fieldName : dirtyFields) {
            Field field = entityClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            builder.set(fieldName, "?");
            params.add(field.get(entity));
        }
        params.add(id);

        String sql = builder.build();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            pstmt.executeUpdate();
        }
    }

    private void executeInsert(Object entity, Class<?> entityClass, String tableName) throws Exception {
        InsertQueryBuilder builder = new InsertQueryBuilder().into(tableName);
        List<Object> params = new ArrayList<>();

        for (Field field : entityClass.getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(entity);
            if (value == null) {
                continue;
            }
            builder.value(field.getName(), "?");
            params.add(value);
        }

        String sql = builder.build();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            pstmt.executeUpdate();
        }
    }

    private long getNextId(String tableName) throws SQLException {
        String sql = "SELECT MAX(id) FROM " + tableName;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                long maxId = rs.getLong(1);
                return rs.wasNull() ? 1L : maxId + 1;
            }
            return 1L;
        }
    }

    private String resolveTableName(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table != null && !table.name().isEmpty()) {
            return table.name();
        }
        return entityClass.getSimpleName().toLowerCase();
    }

    private String resolveIdColumnName(Class<?> entityClass) {
        return resolveIdField(entityClass).getName();
    }

    private Field resolveIdField(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        throw new IllegalStateException("@Id 필드를 찾을 수 없습니다: " + entityClass.getName());
    }
}
