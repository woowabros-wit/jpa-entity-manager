package builder;

import builder.where.ComparisonCondition;
import builder.where.WhereClause;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UpdateQueryBuilder implements Query {

    private String table;
    private Map<String, String> columnValues;
    private WhereClause whereClause;

    public UpdateQueryBuilder table(String table) {
        this.table = table;
        return this;
    }

    public UpdateQueryBuilder set(String column, String value) {
        if (columnValues == null) {
            columnValues = new LinkedHashMap<>();
        }
        columnValues.put(column, value);
        return this;
    }

    public UpdateQueryBuilder set(Map<String, String> columnValues) {
        if (this.columnValues == null) {
            this.columnValues = new LinkedHashMap<>();
        }
        this.columnValues.putAll(columnValues);
        return this;
    }

    public UpdateQueryBuilder where(ComparisonCondition condition) {
        whereClause = WhereClause.empty();
        whereClause.where(condition);
        return this;
    }

    public UpdateQueryBuilder and(ComparisonCondition condition) {
        whereClause.and(condition);
        return this;
    }

    public UpdateQueryBuilder or(ComparisonCondition condition) {
        whereClause.or(condition);
        return this;
    }

    @Override
    public String build() {
        if (table == null || table.isBlank()) {
            throw new IllegalStateException("table은 null일 수 없습니다.");
        }
        if (whereClause == null) {
            throw new IllegalStateException("where 조건을 반드시 지정해주세요.");
        }
        return "UPDATE " + table
            + " SET " + generateSetString()
            + " " + whereClause.toSql();
    }

    private String generateSetString() {
        List<String> columnValueStrings = columnValues.entrySet().stream()
            .map(entry -> entry.getKey() + " = " + entry.getValue())
            .toList();
        return String.join(", ", columnValueStrings);
    }
}
