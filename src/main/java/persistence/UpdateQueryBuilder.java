package persistence;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateQueryBuilder {

    private String table = null;
    private Map<String, String> valueMap = new LinkedHashMap<>();
    private String where = null;

    /**
     * UPDATE할 테이블 지정
     */
    public UpdateQueryBuilder table(String table) {
        this.table = table;

        return this;
    }

    /**
     * SET 절에 컬럼-값 추가
     */
    public UpdateQueryBuilder set(String column, String value) {
        valueMap.put(column, value);

        return this;
    }

    /**
     * WHERE 절 추가
     */
    public UpdateQueryBuilder where(String condition) {
        where = condition;

        return this;
    }

    /**
     * SQL 생성
     */
    public String build() {
        StringBuilder query = new StringBuilder("UPDATE ");

        if (!isValidQuery(where)) {
            throw new IllegalStateException("WHERE 조건이 필수입니다.");
        }

        String set = valueMap.entrySet()
                .stream()
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .collect(Collectors.joining(", "));

        return query.append(table)
                .append(" SET ")
                .append(set)
                .append(" WHERE ").append(where)
                .toString();
    }

    private boolean isValidQuery(String where) {
        return where != null && !where.isBlank();
    }
}
