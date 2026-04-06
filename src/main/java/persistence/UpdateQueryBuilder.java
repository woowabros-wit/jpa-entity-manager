package persistence;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class UpdateQueryBuilder {

    private String table;
    private final LinkedHashMap<String, String> setValues = new LinkedHashMap<>();
    private String condition;

    public UpdateQueryBuilder table(String table) {
        this.table = table;
        return this;
    }

    public UpdateQueryBuilder set(String column, String value) {
        setValues.put(column, value);
        return this;
    }

    public UpdateQueryBuilder where(String condition) {
        this.condition = condition;
        return this;
    }

    public String build() {
        if (table == null || table.isBlank()) {
            throw new IllegalStateException("테이블 지정(table)은 필수");
        }
        if (setValues.isEmpty()) {
            throw new IllegalArgumentException("최소 1개 이상의 SET 컬럼-값 쌍이 필요");
        }
        if (condition == null || condition.isBlank()) {
            throw new IllegalStateException("WHERE 조건은 필수");
        }

        String setClauses = setValues.entrySet().stream()
                .map(e -> e.getKey() + " = " + e.getValue())
                .collect(Collectors.joining(", "));

        return "UPDATE " + table + " SET " + setClauses + " WHERE " + condition;
    }
}
