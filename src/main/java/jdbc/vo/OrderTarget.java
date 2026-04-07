package jdbc.vo;

public class OrderTarget {

    private final String name;
    private final String operand;

    public OrderTarget(String name, String operand) {
        this.name = name;
        this.operand = operand;
    }

    public String getOrderString() {
        return "ORDER BY " + name + " " + operand;
    }
}
