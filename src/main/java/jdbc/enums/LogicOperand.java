package jdbc.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum LogicOperand {

    AND("AND"),
    OR("OR"),
    DEFAULT(null)
    ;

    private final String operandValue;

    LogicOperand(String operandValue) {
        this.operandValue = operandValue;
    }

    public static LogicOperand from(String operandValue) {
        validateOperand(operandValue);
        if (operandValue.contains(LogicOperand.AND.operandValue)) {
            return AND;
        }
        if (operandValue.contains(LogicOperand.OR.operandValue)) {
            return OR;
        }
        return DEFAULT;
    }

    private static void validateOperand(String operandValue) {
        boolean isAndExists = operandValue.contains(LogicOperand.AND.operandValue);
        boolean isOrExists = operandValue.contains(LogicOperand.OR.operandValue);

        if (isAndExists && isOrExists) {
            throw new IllegalArgumentException("AND와 OR 중 하나만 있어야 함");
        }
    }

    public List<String> parse(String operandValue) {
        if (this == DEFAULT) {
            return Collections.singletonList(operandValue);
        }
        String[] parsedResult = operandValue.split(this.operandValue);
        boolean isBlankExist = Arrays.stream(parsedResult).anyMatch(String::isBlank);
        if (isBlankExist) {
            throw new IllegalArgumentException("올바르지 않은 " + this.operandValue);
        }
        return Arrays.asList(parsedResult);
    }

    public String getOperandValue() {
        return operandValue;
    }
}
