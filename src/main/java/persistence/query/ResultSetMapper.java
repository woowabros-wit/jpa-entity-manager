package persistence.query;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultSetMapper {

    public <T> T mapToObject(ResultSet rs, Class<T> targetClass) throws Exception {
        T instance = targetClass.getDeclaredConstructor().newInstance();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        Map<String, Field> fieldMap = getFieldMap(targetClass);

        for (int i = 1; i <= columnCount; i++) {
            String columnLabel = metaData.getColumnLabel(i);

            String fieldName = convertToCamelCase(columnLabel);

            Field field = fieldMap.get(fieldName);
            if (field != null) {
                field.setAccessible(true);

                Object value = getConvertedValue(rs, i, field.getType());
                field.set(instance, value);
            }
        }

        return instance;
    }

    public <T> List<T> mapToList(ResultSet rs, Class<T> targetClass) throws Exception {
        List<T> list = new ArrayList<>();

        while (rs.next()) {
            T object = mapToObject(rs, targetClass);
            list.add(object);
        }

        return list;
    }

    private <T> Map<String, Field> getFieldMap(Class<T> targetClass) {
        Map<String, Field> fieldMap = new HashMap<>();

        Class<?> currentClass = targetClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                fieldMap.put(field.getName(), field);
            }
            currentClass = currentClass.getSuperclass(); // 부모 클래스까지 처리
        }

        return fieldMap;
    }

    private String convertToCamelCase(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }

        StringBuilder camelCase = new StringBuilder();
        boolean capitalizeNext = false;

        char[] snakeCaseWithLowerCaseArray = snakeCase.toLowerCase().toCharArray();
        for (char c : snakeCaseWithLowerCaseArray) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    camelCase.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    camelCase.append(c);
                }
            }
        }

        return camelCase.toString();
    }

    private Object getConvertedValue(ResultSet rs, int columnIndex, Class<?> targetType) throws SQLException {
        Object value = rs.getObject(columnIndex);

        if (value == null) {
            return null;
        }

        if (targetType == String.class) {
            return rs.getString(columnIndex);
        }

        if (targetType == Integer.class || targetType == int.class) {
            return rs.getInt(columnIndex);
        }

        if (targetType == Long.class || targetType == long.class) {
            long longValue = rs.getLong(columnIndex);
            if (targetType == Long.class && rs.wasNull()) {
                return null;
            }
            return longValue;
        }

        if (targetType == Boolean.class || targetType == boolean.class) {
            boolean boolValue = rs.getBoolean(columnIndex);
            if (targetType == Boolean.class && rs.wasNull()) {
                return null;
            }
            return boolValue;
        }

        if (targetType == Double.class || targetType == double.class) {
            double doubleValue = rs.getDouble(columnIndex);
            if (targetType == Double.class && rs.wasNull()) {
                return null;
            }
            return doubleValue;
        }

        if (targetType == Float.class || targetType == float.class) {
            float floatValue = rs.getFloat(columnIndex);
            if (targetType == Float.class && rs.wasNull()) {
                return null;
            }
            return floatValue;
        }

        if (targetType == LocalDateTime.class) {
            Timestamp timestamp = rs.getTimestamp(columnIndex);
            return timestamp != null ? timestamp.toLocalDateTime() : null;
        }

        return value;
    }
}