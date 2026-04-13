package persistence.query;

import java.util.LinkedHashMap;
import java.util.Map;

public class UpdateQueryBuilder {
    private String tableName;
    private Map<String, String> columnValues = new LinkedHashMap<>();
    private String whereCondition;

    public UpdateQueryBuilder table(String table) {
        this.tableName = table;
        return this;
    }

    public UpdateQueryBuilder set(String column, String value) {
        columnValues.put(column, value);
        return this;
    }

    public UpdateQueryBuilder set(Map<String, String> columnValues) {
        this.columnValues.putAll(columnValues);
        return this;
    }

    public UpdateQueryBuilder where(String condition) {
        this.whereCondition = condition;
        return this;
    }

    public String build() {
        if (tableName == null) {
            throw new IllegalStateException();
        }

        if (columnValues.isEmpty()) {
            throw new IllegalStateException();
        }

        if (whereCondition == null || whereCondition.isEmpty()) {
            throw new IllegalStateException();
        }

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(tableName);

        sql.append(" SET ");
        String[] setPairs = columnValues.entrySet().stream()
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .toArray(String[]::new);
        sql.append(String.join(", ", setPairs));

        sql.append(" WHERE ").append(whereCondition);

        return sql.toString();
    }
}