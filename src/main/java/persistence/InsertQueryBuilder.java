package persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsertQueryBuilder {


    private String table;
    private final Map<String, String> columns = new HashMap<>();

    /**
     * INSERT할 테이블 지정
     */
    public InsertQueryBuilder into(String table) {
        this.table = table;
        return this;
    }

    ;

    /**
     * 컬럼-값 쌍 추가
     *
     * @param column 컬럼명
     * @param value  값 (?, 파라미터 플레이스홀더)
     */
    public InsertQueryBuilder value(String column, String value) {
        columns.put(column, value);
        return this;
    }

    ;

    /**
     * 여러 컬럼-값 쌍을 Map으로 추가
     */
    public InsertQueryBuilder values(Map<String, String> columnValues) {
        columns.clear();
        columns.putAll(columnValues);
        return this;
    }

    /**
     * SQL 생성
     */
    public String build() {

        if (table == null || table.isBlank()) {
            throw new IllegalStateException("테이블 지정(into)은 필수입니다.");
        }
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("최소 1개 이상의 컬럼-값 쌍이 필요합니다.");
        }

        StringBuilder sb = new StringBuilder();

        List<String> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();
        columns.forEach((k, v) -> {
            keys.add(k);
            values.add(v);
        });

        return sb.append("INSERT INTO ")
                .append(table)
                .append(" (")
                .append(String.join(", ", keys))
                .append(")")
                .append(" VALUES (")
                .append(String.join(", ", values))
                .append(")")
                .toString();
    }
}
