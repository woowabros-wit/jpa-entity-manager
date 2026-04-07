package builder;

import builder.where.ComparisonCondition;
import builder.where.WhereClause;

public class DeleteQueryBuilder implements Query {

    private String table;
    private WhereClause whereClause;

    public DeleteQueryBuilder from(String table) {
        this.table = table;
        return this;
    }

    public DeleteQueryBuilder where(ComparisonCondition condition) {
        whereClause = WhereClause.empty();
        whereClause.where(condition);
        return this;
    }

    public DeleteQueryBuilder and(ComparisonCondition condition) {
        whereClause.and(condition);
        return this;
    }

    public DeleteQueryBuilder or(ComparisonCondition condition) {
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
        return "DELETE FROM " + table
            + " " + whereClause.toSql();
    }
}
