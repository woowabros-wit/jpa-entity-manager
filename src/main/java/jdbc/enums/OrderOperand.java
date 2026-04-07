package jdbc.enums;

import jdbc.vo.OrderTarget;

import java.util.Arrays;

public enum OrderOperand {

    ASCENDING("ASC"),
    DESCENDING("DESC")
    ;

    private final String operandValue;

    OrderOperand(String operandValue) {
        this.operandValue = operandValue;
    }

    public static OrderTarget parse(String orderName, String operandValue) {
        validateOperand(orderName);
        OrderOperand orderOperand;
        orderOperand = Arrays.stream(OrderOperand.values())
                .filter(it -> it.operandValue.equals(operandValue))
                .findFirst()
                .orElse(ASCENDING);

        return new OrderTarget(orderName, orderOperand.operandValue);
    }

    private static void validateOperand(String orderName) {
        if (orderName == null) {
            throw new IllegalArgumentException("정렬 기준 없이 정렬 불가: " + orderName);
        }
    }
}
