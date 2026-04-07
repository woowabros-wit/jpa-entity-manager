package jdbc.enums;

import jdbc.vo.CompareTarget;

import java.util.*;

public enum CompareOperand {

    GREATER_OR_EQUAL(">=", 1),
    LESS_OR_EQUAL("<=", 1),
    NOT_EQUAL("!=", 1),
    GREATER(">", 2),
    LESS("<", 2),
    EQUAL("=", 2),
    ;

    private final String operandValue;
    private final int priority;

    CompareOperand(String operandValue, int priority) {
        this.operandValue = operandValue;
        this.priority = priority;
    }

    public static CompareOperand from(String operandValue) {
        validOperand(operandValue);
        return Arrays.stream(CompareOperand.values())
                .sorted(Comparator.comparing(it -> it.priority))
                .filter(it -> operandValue.contains(it.operandValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("올바르지 않은 operand: " + operandValue));
    }

    private static void validOperand(String operandValue) {
        Map<Character, Integer> charMap = new HashMap<>();
        for (char c : operandValue.toCharArray()) {
            charMap.put(c, charMap.getOrDefault(c, 0) + 1);
        }
        List<Character> charList = Arrays.asList('=', '<', '>', '!');
        for (char c : charList) {
            if (charMap.containsKey(c) &&  charMap.get(c) > 1) {
                throw new IllegalArgumentException("Invalid operand: " + operandValue);
            }
        }
    }

    public CompareTarget parse(String operandValue, LogicOperand logicOperand) {
        String[] parsedResult = operandValue.split(this.operandValue);
        if (parsedResult.length != 2) {
            throw new IllegalArgumentException("column과 value가 있어야 함" + operandValue);
        }
        String column = parsedResult[0].trim();
        String value = parsedResult[1].trim();

        if (logicOperand == LogicOperand.AND) {
            return new CompareTarget(column, value, this.operandValue, logicOperand.getOperandValue());
        }
        if (logicOperand == LogicOperand.OR) {
            return new CompareTarget(column, value, this.operandValue, logicOperand.getOperandValue());
        }
        return new CompareTarget(column, value, this.operandValue, logicOperand.getOperandValue());
    }
}
