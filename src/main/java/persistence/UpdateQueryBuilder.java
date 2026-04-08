package persistence;

import java.util.LinkedHashMap;
import java.util.Map;

public class UpdateQueryBuilder {

    private String table;
    private final Map<String, String> columnValues = new LinkedHashMap<>();
    private String whereCondition;

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
        this.columnValues.put(column, value);
        return this;
    }

    /**
     * WHERE 절 추가
     */

    public UpdateQueryBuilder where(String condition) {
        this.whereCondition = condition;
        return this;
    }

    /**
     * SQL 생성
     */
    public String build() {
        if (table == null || table.isEmpty()) {
            throw new IllegalStateException("테이블 이름이 지정되지 않았습니다.");
        }

        if (columnValues.isEmpty()) {
            throw new IllegalStateException("SET 절에 컬럼-값 쌍이 없습니다.");
        }

        if (whereCondition == null || whereCondition.isEmpty()) {
            throw new IllegalStateException("WHERE 조건이 지정되지 않았습니다. UPDATE 쿼리는 WHERE 절이 필수입니다.");
        }
        return "UPDATE " + table + " SET " + buildSetClause() + " WHERE " + whereCondition;
    }

    private String buildSetClause() {
        StringBuilder setClause = new StringBuilder();
        for (Map.Entry<String, String> entry : columnValues.entrySet()) {
            if (!setClause.isEmpty()) {
                setClause.append(", ");
            }
            setClause.append(entry.getKey()).append(" = ").append(entry.getValue());
        }
        return setClause.toString();
    }
}

