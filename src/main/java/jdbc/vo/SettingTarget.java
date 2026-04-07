package jdbc.vo;

public class SettingTarget extends NamedParameterTarget {

    private final String name;
    private final String displayValue;

    public SettingTarget(String name, String displayValue) {
        this.name = name;
        this.displayValue = displayValue;
        super.validate(displayValue);
        super.setNamedParameter(displayValue);
    }

    public String getTargetString() {
        if (!super.isNamedParameterEmpty()) {
            return name + " = ?";
        }
        return name + " = " + displayValue;
    }

    public String getName() {
        return name;
    }

    public String getDisplayValue() {
        if (!super.isNamedParameterEmpty()) {
            return "?";
        }
        return displayValue;
    }
}
