package jdbc.vo;

public class NamedParameterBindCommand {

    private final int order;
    private final Object value;

    public NamedParameterBindCommand(int order, Object value) {
        this.order = order;
        this.value = value;
    }

    public int getOrder() {
        return order;
    }

    public Object getValue() {
        return value;
    }
}
