package persistence;

import java.util.LinkedHashMap;
import java.util.Map;

public class InsertQueryBuilder {

    private String table;
    private final Map<String, String> columnValues = new LinkedHashMap<>();

    /**
     * INSERT할 테이블 지정
     */
    public InsertQueryBuilder into(String table) {
        this.table = table;
        return this;
    }

    /**
     * 컬럼-값 쌍 추가
     *
     * @param column 컬럼명
     * @param value  값 (?, 파라미터 플레이스홀더)
     */
    public InsertQueryBuilder value(String column, String value) {
        this.columnValues.put(column, value);
        return this;
    }

    /**
     * 여러 컬럼-값 쌍을 Map으로 추가
     */
    public InsertQueryBuilder values(Map<String, String> columnValues) {
        this.columnValues.putAll(columnValues);
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
            throw new IllegalStateException("컬럼-값 쌍이 없습니다. 최소 1개 이상의 컬럼-값 쌍이 필요합니다.");
        }
        return String.format("INSERT INTO %s (%s) VALUES (%s)",
                table,
                buildColumns(),
                buildValues()
        );
    }

    private String buildColumns() {
        StringBuilder columns = new StringBuilder();
        for (String column : columnValues.keySet()) {
            if (!columns.isEmpty()) {
                columns.append(", ");
            }
            columns.append(column);
        }
        return columns.toString();
    }

    private String buildValues() {
        StringBuilder values = new StringBuilder();
        for (String value : columnValues.values()) {
            if (!values.isEmpty()) {
                values.append(", ");
            }
            values.append(value);
        }
        return values.toString();
    }
}

