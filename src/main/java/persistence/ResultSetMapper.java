package persistence;

import java.sql.ResultSet;
import java.util.List;


public class ResultSetMapper {

    /**
     * ResultSet의 현재 행을 객체로 변환
     *
     * @param rs          ResultSet (이미 next() 호출된 상태)
     * @param targetClass 변환할 클래스
     * @return 변환된 객체
     */
    public <T> T mapToObject(ResultSet rs, Class<T> targetClass) throws Exception {
        T instance = targetClass.getDeclaredConstructor().newInstance();
        int columnCount = rs.getMetaData().getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = rs.getMetaData().getColumnLabel(i);
            Object value = rs.getObject(i);
            String fieldName = convertToCamelCase(columnName);
            try {
                var field = targetClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(instance, value);
            } catch (NoSuchFieldException e) {
                // 매핑 대상 클래스에 해당 필드가 없으면 무시
            }
        }
        return instance;

    }

    private String convertToCamelCase(String columnName) {
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (char c : columnName.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        return result.toString();
    }

    /**
     * ResultSet의 모든 행을 리스트로 변환
     *
     * @param rs          ResultSet
     * @param targetClass 변환할 클래스
     * @return 변환된 객체 리스트
     */
    public <T> List<T> mapToList(ResultSet rs, Class<T> targetClass) throws Exception {
        List<T> list = new java.util.ArrayList<>();
        while (rs.next()) {
            list.add(mapToObject(rs, targetClass));
        }
        return list;
    }
}
