package builder.where;

public class WhereClause {

    private Condition condition;

    public static WhereClause empty() {
        return new WhereClause();
    }

    public WhereClause where(ComparisonCondition condition) {
        this.condition = condition;
        return this;
    }

    public WhereClause and(ComparisonCondition condition) {
        validateConditionNotNull();
        this.condition = new LogicalCondition(this.condition, condition, LogicalOperator.AND);
        return this;
    }

    public WhereClause and(WhereClause whereClause) {
        validateConditionNotNull();
        this.condition = new LogicalCondition(this.condition, whereClause.condition, LogicalOperator.AND);
        return this;
    }

    public WhereClause or(ComparisonCondition condition) {
        validateConditionNotNull();
        this.condition = new LogicalCondition(this.condition, condition, LogicalOperator.OR);
        return this;
    }

    public WhereClause or(WhereClause whereClause) {
        validateConditionNotNull();
        this.condition = new LogicalCondition(this.condition, whereClause.condition, LogicalOperator.OR);
        return this;
    }

    private void validateConditionNotNull() {
        if (this.condition == null) {
            throw new IllegalStateException("WHERE 를 우선 지정해주세요.");
        }
    }

    public String toSql() {
        if (condition == null) {
            return "";
        }

        return "WHERE " + String.join(" ", condition.generateSqlString());
    }
}
