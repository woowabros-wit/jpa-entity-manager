package jdbc;

import jdbc.vo.ColumnInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.sql.Types.*;

public class ResultSetMapper {

    /**
     * ResultSet의 현재 행을 객체로 변환
     * @param rs ResultSet (이미 next() 호출된 상태)
     * @param targetClass 변환할 클래스
     * @return 변환된 객체
     */
    public <T> T mapToObject(ResultSet rs, Class<T> targetClass) throws Exception {
        Map<String, Class<?>> fieldMap = generateFieldInfoMap(targetClass);
        Map<String, ColumnInfo> columnMap = generateColumnInfoMap(rs);
        validate(fieldMap, columnMap);
        return makeInstance(targetClass, columnMap);
    }

    private Map<String, Class<?>> generateFieldInfoMap(Class<?> targetClass) {
        return Arrays.stream(targetClass.getDeclaredFields())
                .collect(Collectors.toMap(Field::getName, Field::getType));
    }

    private Map<String, ColumnInfo> generateColumnInfoMap(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        Map<String, ColumnInfo> columnInfoMap = new HashMap<>();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = snakeToCamel(metaData.getColumnName(i));
            Class<?> columnType = convertColumnClass(metaData.getColumnType(i));
            Object columnValue = rs.getObject(i);
            if (columnValue instanceof Timestamp timestamp) {
                columnValue = timestamp.toLocalDateTime();
            }
            if (columnValue instanceof java.sql.Date date) {
                columnValue = date.toLocalDate();
            }
            if (columnValue instanceof Time time) {
                columnValue = time.toLocalTime();
            }
            columnInfoMap.put(columnName, new ColumnInfo(columnType, columnValue));
        }
        return columnInfoMap;
    }

    private String snakeToCamel(String snake) {
        if (snake == null) return null;
        return Pattern.compile("_+([a-z])")
                .matcher(snake.toLowerCase())
                .replaceAll(mr -> mr.group(1).toUpperCase());
    }

    private Class<?> convertColumnClass(int type) {
        return switch (type) {
            case CHAR, VARCHAR, LONGVARCHAR -> String.class;
            case INTEGER -> Integer.class;
            case BIGINT -> Long.class;
            case FLOAT, DOUBLE -> Double.class;
            case BIT, BOOLEAN, TINYINT -> Boolean.class;
            case TIMESTAMP -> LocalDateTime.class;
            case DATE -> LocalDate.class;
            case TIME -> LocalTime.class;
            default -> throw new IllegalArgumentException("지원하지 않는 칼럼 타입임");
        };
    }

    private void validate(Map<String, Class<?>> fieldMap, Map<String, ColumnInfo> columnMap) {
        Set<String> fieldName = new HashSet<>(fieldMap.keySet());
        Set<String> columnName = new HashSet<>(columnMap.keySet());
        boolean notExistInColumn = fieldName.stream()
                .anyMatch(
                        it -> !columnName.contains(it)
                );

        if (notExistInColumn) {
            throw new IllegalArgumentException("필드에는 있지만 칼럼에 없음");
        }

        boolean isDifferentTypeExists = fieldMap.keySet().stream()
                .anyMatch(
                        it -> {
                            Class<?> fieldClassType = fieldMap.get(it);
                            Class<?> columnClassType = columnMap.get(it).getClazz();

                            return fieldClassType != columnClassType;
                        }
                );
        if (isDifferentTypeExists) {
            throw new IllegalArgumentException("필드 타입과 칼럼 타입이 다름");
        }
    }

    private <T> T makeInstance(Class<T> targetClass, Map<String, ColumnInfo> columnMap) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<?> constructor = targetClass.getDeclaredConstructor();
        if (constructor == null) {
            throw new IllegalStateException("기본 생성자가 없음");
        }
        T instance = (T) constructor.newInstance();
        Arrays.stream(targetClass.getDeclaredFields())
                .forEach(field -> {
                    field.setAccessible(true);
                    ColumnInfo columnInfo = columnMap.get(field.getName());
                    try {
                        field.set(instance, columnInfo.getValue());
                    } catch (Exception e) {
                        throw new IllegalArgumentException("인스턴스 생성 실패 " + e.getMessage());
                    }
                });
        return instance;
    }

    /**
     * ResultSet의 모든 행을 리스트로 변환
     * @param rs ResultSet
     * @param targetClass 변환할 클래스
     * @return 변환된 객체 리스트
     */
    public <T> List<T> mapToList(ResultSet rs, Class<T> targetClass) throws Exception {
        List<T> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapToObject(rs, targetClass));
        }
        return list;
    }
}
