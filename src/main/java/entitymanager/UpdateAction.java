package entitymanager;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import persistence.query.UpdateQueryBuilder;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class UpdateAction implements EntityAction {
    private final Connection connection;
    private final Object entity;
    private final Object id;
    private final List<String> dirtyFields;

    public UpdateAction(Connection connection, Object entity, Object id, List<String> dirtyFields) {
        this.connection = connection;
        this.entity = entity;
        this.id = id;
        this.dirtyFields = dirtyFields;
    }

    @Override
    public void execute() throws Exception {
        Class<?> entityClass = entity.getClass();
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

    private String resolveTableName(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table != null && !table.name().isEmpty()) {
            return table.name();
        }
        return entityClass.getSimpleName().toLowerCase();
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
