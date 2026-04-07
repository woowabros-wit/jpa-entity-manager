package jdbc.vo;

public class ReserveWord {

    private final String value;
    private final boolean isRequired;
    private final int priority;

    public ReserveWord(String value, boolean isRequired, int priority) {
        this.value = value.toUpperCase();
        this.isRequired = isRequired;
        this.priority = priority;
    }

    public String getValue() {
        return value;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public int getPriority() {
        return priority;
    }
}
