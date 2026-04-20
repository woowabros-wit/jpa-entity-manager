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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SimpleEntityManager {
    private final Connection connection;
    private final PersistenceContext persistenceContext;
    private final ActionQueue actionQueue;
    private final EntityTransaction transaction;
    private final ResultSetMapper resultSetMapper;

    public SimpleEntityManager(Connection connection) {
        this.connection = connection;
        this.persistenceContext = new PersistenceContext();
        this.actionQueue = new ActionQueue();
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
        actionQueue.addInsertion(new InsertAction(connection, entity));
    }

    public void flush() throws Exception {
        detectDirtyEntities();
        executeActionQueue();
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

    private void detectDirtyEntities() throws IllegalAccessException {
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
                    actionQueue.addUpdate(new UpdateAction(connection, entity, id, dirtyFields));
                }
            }
        }
    }

    private void executeActionQueue() throws Exception {
        actionQueue.executeAll();
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
