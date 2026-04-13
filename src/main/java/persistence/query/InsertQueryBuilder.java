package persistence.query;

import java.util.LinkedHashMap;
import java.util.Map;

public class InsertQueryBuilder {
    private String tableName;
    private Map<String, String> columnValues = new LinkedHashMap<>(); // LinkedHashMap을 사용하여 컬럼 순서를 보장

    public InsertQueryBuilder into(String table) {
        this.tableName = table;
        return this;
    }

    /**
     * 컬럼-값 쌍 추가
     *
     * @param column 컬럼명
     * @param value  값 (?, 파라미터 플레이스홀더)
     */
    public InsertQueryBuilder value(String column, String value) {
        columnValues.put(column, value);
        return this;
    }

    /**
     * 여러 컬럼-값 쌍을 Map으로 추가
     */
    public InsertQueryBuilder values(Map<String, String> columnValues) {
        this.columnValues.putAll(columnValues);
        return this;
    }

    public String build() {
        if (tableName == null) {
            throw new IllegalStateException();
        }

        if (columnValues.isEmpty()) {
            throw new IllegalStateException();
        }

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableName);

        sql.append(" (");
        sql.append(String.join(", ", columnValues.keySet()));
        sql.append(")");

        sql.append(" VALUES (");
        sql.append(String.join(", ", columnValues.values()));
        sql.append(")");

        return sql.toString();
    }
}