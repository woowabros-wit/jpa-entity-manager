package persistence;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PersistenceContext {

    private final Map<EntityKey, EntityEntry> entries = new LinkedHashMap<>();

    public <T> T get(Class<T> entityClass, Object key) {
        EntityEntry entry = entries.get(new EntityKey(entityClass, key));
        return entry == null ? null : (T) entry.getEntity();
    }

    public <T> void save(Class<T> entityClass, Object key, T entity) {
        EntityKey entityKey = new EntityKey(entityClass, key);
        Object[] snapshot = extractState(entity);
        entries.put(entityKey, new EntityEntry(entity, snapshot));
    }

    public void flush(Connection connection) throws Exception {
        for (EntityEntry entry : entries.values()) {
            Object entity = entry.getEntity();
            Object[] currentState = extractState(entity);

            if (!entry.isDirty(currentState)) {
                continue;
            }

            int[] dirtyFields = entry.findModified(currentState);

            // UPDATE 실행
            update(entity, dirtyFields, connection);
        }
    }

    private Object[] extractState(Object entity) {
        EntityMetadata metadata = EntityMetadata.of(entity.getClass());
        List<Field> fields = metadata.getFields();
        Object[] states = new Object[fields.size()];

        for (int i = 0; i < fields.size(); i++) {
            states[i] = readField(fields.get(i), entity);
        }
        return states;
    }

    private void update(Object entity, int[] dirtyFields, Connection connection)
            throws Exception {
        EntityMetadata metadata = EntityMetadata.of(entity.getClass());
        List<Field> fields = metadata.getFields();

        UpdateQueryBuilder builder = new UpdateQueryBuilder()
                .table(metadata.getTableName());
        for (int index : dirtyFields) {
            builder.set(metadata.getColumnName(fields.get(index)), "?");
        }
        builder.where(metadata.getIdColumnName() + " = ?");
        String sql = builder.build();

        PreparedStatement pstmt = connection.prepareStatement(sql);

        int paramIndex = 1;
        for (int index : dirtyFields) {
            Field field = fields.get(index);
            field.setAccessible(true);
            pstmt.setObject(paramIndex++, field.get(entity));
        }

        // ID 바인딩
        Field idField = metadata.getIdField();
        idField.setAccessible(true);
        pstmt.setObject(paramIndex, idField.get(entity));

        pstmt.executeUpdate();

        System.out.println("[Dirty Checking] UPDATE executed: " + sql);
    }

    private Object readField(Field field, Object target) {
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("필드 접근 실패: " + field.getName(), e);
        }
    }
}
