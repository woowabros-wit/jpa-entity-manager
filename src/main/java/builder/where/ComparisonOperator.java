package builder.where;

public enum ComparisonOperator {
    IS_NULL(
        args -> isEqualArgsSize(0, args),
        args -> "IS NULL"
    ),
    IS_NOT_NULL(
        args -> isEqualArgsSize(0, args),
        args -> "IS NOT NULL"
    ),
    EQ(
        args -> isEqualArgsSize(1, args),
        args -> "= " + args[0]
    ),
    NE(
        args -> isEqualArgsSize(1, args),
        args -> "!= " + args[0]
    ),
    GT(
        args -> isEqualArgsSize(1, args),
        args -> "> " + args[0]
    ),
    LT(
        args -> isEqualArgsSize(1, args),
        args -> "< " + args[0]
    ),
    GTE(
        args -> isEqualArgsSize(1, args),
        args -> ">= " + args[0]
    ),
    LTE(
        args -> isEqualArgsSize(1, args),
        args -> "<= " + args[0]
    ),
    LIKE(
        args -> isEqualArgsSize(1, args),
        args -> "LIKE '" + args[0] + "'"
    ),
    BETWEEN(
        args -> isEqualArgsSize(2, args),
        args -> "BETWEEN " + args[0] + " AND " + args[1]
    ),
    IN(
        ComparisonOperator::isEmpty,
        args -> "IN (" + String.join(", ", args) + ")"
    ),
    NOT_IN(
        ComparisonOperator::isEmpty,
        args -> "NOT IN (" + String.join(", ", args) + ")"
    ),
    ;

    private static void isEqualArgsSize(int size, String[] args) {
        if (args == null || args.length != size) {
            throw new IllegalArgumentException(String.format("해당 연산자의 인자는 %d개만 입력 가능합니다.", size));
        }
    }

    private static void isEmpty(String[] args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("해당 연산자는 최소한 하나 이상의 인자가 필요합니다.");
        }
    }

    private final OperatorValidator operatorValidator;
    private final SqlGenerator sqlGenerator;

    ComparisonOperator(OperatorValidator operatorValidator, SqlGenerator sqlGenerator) {
        this.operatorValidator = operatorValidator;
        this.sqlGenerator = sqlGenerator;
    }

    public void validate(String... args) {
        operatorValidator.validate(args);
    }

    public String toSqlString(String column, String... args) {
        return column + " " + sqlGenerator.generate(args);
    }

    @FunctionalInterface
    private interface OperatorValidator {

        void validate(String... args);
    }

    @FunctionalInterface
    private interface SqlGenerator {

        String generate(String... args);
    }
}
