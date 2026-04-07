package builder.where;

public class ComparisonCondition implements Condition {

    private String column;
    private ComparisonOperator comparisonOperator;
    private String[] values;

    public ComparisonCondition(String column, ComparisonOperator comparisonOperator, String... values) {
        if (column == null || column.isBlank()) {
            throw new IllegalStateException("컬럼명은 null 또는 빈 문자열일 수 없습니다.");
        }
        comparisonOperator.validate(values);

        this.column = column;
        this.comparisonOperator = comparisonOperator;
        this.values = values;
    }

    @Override
    public String generateSqlString() {
        return comparisonOperator.toSqlString(column, values);
    }
}
