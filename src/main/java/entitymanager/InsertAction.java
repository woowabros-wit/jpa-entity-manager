package entitymanager;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import persistence.query.InsertQueryBuilder;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InsertAction implements EntityAction {
    private final Connection connection;
    private final Object entity;

    public InsertAction(Connection connection, Object entity) {
        this.connection = connection;
        this.entity = entity;
    }

    @Override
    public void execute() throws Exception {
        Class<?> entityClass = entity.getClass();
        String tableName = resolveTableName(entityClass);
        boolean hasGeneratedId = hasGeneratedValue(entityClass);

        InsertQueryBuilder builder = new InsertQueryBuilder().into(tableName);
        List<Object> params = new ArrayList<>();

        for (Field field : entityClass.getDeclaredFields()) {
            field.setAccessible(true);

            if (hasGeneratedId && field.isAnnotationPresent(Id.class)) {
                continue;
            }

            Object value = field.get(entity);
            if (value == null) {
                continue;
            }
            builder.value(field.getName(), "?");
            params.add(value);
        }

        String sql = builder.build();

        if (hasGeneratedId) {
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bindParameters(pstmt, params);
                pstmt.executeUpdate();

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        Field idField = resolveIdField(entityClass);
                        idField.setAccessible(true);
                        idField.set(entity, keys.getLong(1));
                    }
                }
            }
        } else {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                bindParameters(pstmt, params);
                pstmt.executeUpdate();
            }
        }
    }

    private void bindParameters(PreparedStatement pstmt, List<Object> params) throws Exception {
        for (int i = 0; i < params.size(); i++) {
            pstmt.setObject(i + 1, params.get(i));
        }
    }

    private boolean hasGeneratedValue(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(GeneratedValue.class)) {
                return true;
            }
        }
        return false;
    }

    private Field resolveIdField(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        throw new IllegalStateException("@Id 필드를 찾을 수 없습니다: " + entityClass.getName());
    }

    private String resolveTableName(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table != null && !table.name().isEmpty()) {
            return table.name();
        }
        return entityClass.getSimpleName().toLowerCase();
    }
}
