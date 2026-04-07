package jdbc.vo;

public class CompareTarget extends NamedParameterTarget {

    private final String name;
    private final String displayValue;
    private final String compareOperand;
    private final String joinValue;

    public CompareTarget(String name, String displayValue, String compareOperand, String joinValue) {
        this.name = name;
        this.displayValue = displayValue;
        this.compareOperand = compareOperand;
        this.joinValue = joinValue;
        super.validate(displayValue);
        super.setNamedParameter(displayValue);
    }

    public String getTargetString() {
        if (!super.isNamedParameterEmpty()) {
            return name + " " + compareOperand + " ?";
        }
        return name + " " + compareOperand + " " + displayValue;
    }

    public String getJoinString() {
        if (joinValue == null) {
            return "";
        }
        return " " + joinValue + " ";
    }
}
