package persistence;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ResultSetMapper {

    public <T> T mapToObject(ResultSet resultSet, Class<T> targetClass) throws Exception {
        Constructor<T> constructor = targetClass.getDeclaredConstructor();
        T instance = constructor.newInstance();

        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            String fieldName = convertToCamelCase(columnName);

            Field field;
            try {
                field = targetClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                continue;
            }

            field.setAccessible(true);
            Object value = resultSet.getObject(i);
            if (value != null) {
                field.set(instance, convertType(value, field.getType()));
            }
        }

        return instance;
    }

    public <T> List<T> mapToList(ResultSet rs, Class<T> targetClass)
            throws Exception {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(mapToObject(rs, targetClass));
        }
        return results;
    }

    private Object convertType(Object value, Class<?> type) {
        if (type.isInstance(value)) {
            return value;
        }
        if (type == int.class || type == Integer.class) {
            return ((Number) value).intValue();
        }
        if (type == long.class || type == Long.class) {
            return ((Number) value).longValue();
        }

        if ((type == boolean.class || type == Boolean.class) && value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        if (type == LocalDateTime.class && value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }

        throw new IllegalStateException();
    }

    private String convertToCamelCase(String dbColumn) {
        String lowerCase = dbColumn.toLowerCase();

        StringBuilder camelCase = new StringBuilder();
        boolean nextUpper = false;
        for (char c : lowerCase.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    camelCase.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    camelCase.append(c);
                }
            }
        }

        return camelCase.toString();
    }
}
