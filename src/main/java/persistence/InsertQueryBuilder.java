package persistence;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class InsertQueryBuilder {

    private final StringBuilder query = new StringBuilder("INSERT INTO ");
    private String into = null;
    private Map<String, String> valueMap = new LinkedHashMap<>();

    /**
     * INSERT할 테이블 지정
     */
    public InsertQueryBuilder into(String table) {
        into = table;
        return this;
    }

    /**
     * 컬럼-값 쌍 추가
     * @param column 컬럼명
     * @param value 값 (?, 파라미터 플레이스홀더)
     */
    public InsertQueryBuilder value(String column, String value) {
        valueMap.put(column, value);
        return this;
    }

    /**
     * 여러 컬럼-값 쌍을 Map으로 추가
     */
    public InsertQueryBuilder values(Map<String, String> columnValues) {
        valueMap.putAll(columnValues);
        return this;
    }

    /**
     * SQL 생성
     */
    public String build() {
        if (!isValidQuery(valueMap)) {
            throw new IllegalArgumentException("최소 1개 이상의 컬럼-값 쌍을 가져야 함");
        }

        Set<Map.Entry<String, String>> entries = valueMap.entrySet();

        String key = entries.stream().map(Map.Entry::getKey).collect(Collectors.joining(", "));
        String value = entries.stream().map(Map.Entry::getValue).collect(Collectors.joining(", "));

        return query.append(into)
                .append(" (" + key + ") ")
                .append("VALUES")
                .append(" (" + value + ")")
                .toString();
    }

    private boolean isValidQuery(Map<String, String> valueMap) {
        return valueMap != null && !valueMap.isEmpty();
    }
}
