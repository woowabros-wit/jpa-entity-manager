package mapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ResultSetMapper {

    private static final Map<Class<?>, Function<Number, Object>> NUMBER_CONVERTERS = Map.of(
        int.class, Number::intValue,
        Integer.class, Number::intValue,
        long.class, Number::longValue,
        Long.class, Number::longValue,
        double.class, Number::doubleValue,
        Double.class, Number::doubleValue,
        float.class, Number::floatValue,
        Float.class, Number::floatValue,
        short.class, Number::shortValue,
        Short.class, Number::shortValue
    );

    /**
     * ResultSet의 모든 행을 리스트로 변환
     * @param resultSet ResultSet
     * @param targetClass 변환할 클래스
     * @return 변환된 객체 리스트
     */
    public <T> List<T> mapToList(ResultSet resultSet, Class<T> targetClass) throws Exception {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(mapToObject(resultSet, targetClass));
        }
        return results;
    }

    /**
     * ResultSet의 현재 행을 객체로 변환
     * @param resultSet ResultSet (이미 next() 호출된 상태)
     * @param targetClass 변환할 클래스
     * @return 변환된 객체
     */
    public <T> T mapToObject(ResultSet resultSet, Class<T> targetClass) throws Exception {
        Constructor<T> constructor = targetClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        T instance = constructor.newInstance();

        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            String fieldName = toCamelCase(columnName);

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
            field.setAccessible(false);
        }

        return instance;
    }

    private String toCamelCase(String columnName) {
        StringBuilder result = new StringBuilder();
        char[] columnNameCharArray = columnName.toLowerCase().toCharArray();
        boolean nextUpper = false;

        for (char c : columnNameCharArray) {
            if (c == '_') {
                nextUpper = true;
            } else if (nextUpper) {
                result.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    private Object convertType(Object value, Class<?> targetType) {
        if (targetType.isInstance(value)) {
            return value;
        }
        if (targetType == String.class) {
            return value.toString();
        }
        if (value instanceof Number number && NUMBER_CONVERTERS.containsKey(targetType)) {
            return NUMBER_CONVERTERS.get(targetType).apply(number);
        }
        if (value instanceof Boolean bool && (targetType == boolean.class || targetType == Boolean.class)) {
            return bool;
        }
        if (value instanceof Timestamp timestamp && targetType == LocalDateTime.class) {
            return timestamp.toLocalDateTime();
        }
        throw new IllegalArgumentException("타입 변환 불가: " + value.getClass().getName() + " → " + targetType.getName());
    }
}
