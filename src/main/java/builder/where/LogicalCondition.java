package builder.where;

public class LogicalCondition implements Condition {

    private Condition leftCondition;
    private Condition rightCondition;
    private LogicalOperator logicalOperator;

    public LogicalCondition(Condition leftCondition, Condition rightCondition, LogicalOperator logicalOperator) {
        this.leftCondition = leftCondition;
        this.rightCondition = rightCondition;
        this.logicalOperator = logicalOperator;
    }

    @Override
    public String generateSqlString() {
        return "(" + leftCondition.generateSqlString()+") " + logicalOperator.name() + " (" + rightCondition.generateSqlString() + ")";
    }
}
