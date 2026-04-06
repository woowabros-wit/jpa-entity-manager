package persistence;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ResultSetMapper {

    /**
     * ResultSet의 현재 행을 객체로 변환
     * @param rs ResultSet (이미 next() 호출된 상태)
     * @param targetClass 변환할 클래스
     * @return 변환된 객체
     */

    public <T> T mapToObject(ResultSet rs, Class<T> targetClass)
            throws Exception {

        T instance;
        try {
            instance = targetClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    targetClass.getName() + " 클래스에 기본 생성자가 없습니다.");
        }



        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            String fieldName = snakeToCamel(columnName);
            Field field = findField(targetClass, fieldName);

            if (field == null) {
                continue;
            }
            field.setAccessible(true);
            Object value = rs.getObject(i);
            Object o = convertValue(value, field.getType(), columnName, fieldName);
            field.set(instance, o);
        }
        return instance;
    }

    private Object convertValue(Object value, Class<?> targetType, String columnName, String fieldName) {

        if (value == null) {
            return null;
        }

        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        switch (value) {
            case Number number -> {
                return convertNumber(number, targetType, columnName, fieldName);
            }
            case Timestamp timestamp when targetType == LocalDateTime.class -> {
                return timestamp.toLocalDateTime();
            }
            case Boolean bool -> {
                if (targetType == boolean.class || targetType == Boolean.class) {
                    return bool;
                }
            }
            default -> throw new IllegalArgumentException(
                    String.format("타입 변환 실패: 컬럼 '%s' → 필드 '%s' (%s → %s)",
                            columnName, fieldName, value.getClass().getSimpleName(), targetType.getSimpleName()));
        }
        throw new IllegalArgumentException(
                String.format("타입 변환 실패: 컬럼 '%s' → 필드 '%s' (%s → %s)",
                        columnName, fieldName, value.getClass().getSimpleName(), targetType.getSimpleName()));
    }

    private Object convertNumber(Number number, Class<?> targetType, String columnName, String fieldName) {
        if (targetType == int.class || targetType == Integer.class) {
            return number.intValue();
        }
        if (targetType == long.class || targetType == Long.class) {
            return number.longValue();
        }
        if (targetType == double.class || targetType == Double.class) {
            return number.doubleValue();
        }
        if (targetType == float.class || targetType == Float.class) {
            return number.floatValue();
        }
        if (targetType == short.class || targetType == Short.class) {
            return number.shortValue();
        }
        if (targetType == byte.class || targetType == Byte.class) {
            return number.byteValue();
        }
        throw new IllegalArgumentException(
                String.format("타입 변환 실패: 컬럼 '%s' → 필드 '%s' (%s → %s)",
                        columnName, fieldName, number.getClass().getSimpleName(), targetType.getSimpleName()));
    }

    String snakeToCamel(String snake) {
        String lower = snake.toLowerCase();
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (char c : lower.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else if (nextUpper) {
                sb.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Field findField(Class<?> clazz, String fieldName) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    /**
     * ResultSet의 모든 행을 리스트로 변환
     * @param rs ResultSet
     * @param targetClass 변환할 클래스
     * @return 변환된 객체 리스트
     */
    public <T> List<T> mapToList(ResultSet rs, Class<T> targetClass)
            throws Exception {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(mapToObject(rs, targetClass));
        }
        return results;
    };
}
