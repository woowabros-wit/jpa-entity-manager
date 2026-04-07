package builder;

import java.util.LinkedHashMap;
import java.util.Map;

public class InsertQueryBuilder implements Query {

    private String table;
    private Map<String, String> columnValues;

    public InsertQueryBuilder into(String table) {
        this.table = table;
        return this;
    }

    public InsertQueryBuilder value(String column, String value) {
        initColumnValues();
        columnValues.put(column, value);
        return this;
    }

    public InsertQueryBuilder values(Map<String, String> newColumnValues) {
        initColumnValues();
        columnValues.putAll(newColumnValues);
        return this;
    }

    private void initColumnValues() {
        if (columnValues == null) {
            columnValues = new LinkedHashMap<>();
        }
    }

    @Override
    public String build() {
        if (table == null || table.isBlank()) {
            throw new IllegalStateException("table은 null일 수 없습니다.");
        }
        if (columnValues == null || columnValues.isEmpty()) {
            throw new IllegalStateException("컬럼은 최소 1개 이상 입력되어야 합니다.");
        }
        return "INSERT INTO " + table
            + " (" + String.join(", ", columnValues.keySet()) + ")"
            + " VALUES (" + String.join(", ", columnValues.values()) + ")";
    }
}
