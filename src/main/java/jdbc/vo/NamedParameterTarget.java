package jdbc.vo;

public abstract class NamedParameterTarget {

    protected String namedParameter;
    protected int order;
    protected Object value;

    public String getNamedParameter() {
        return namedParameter;
    }

    public int getOrder() {
        return order;
    }

    public Object getValue() {
        return value;
    }

    protected void validate(String value) {
        if (value.isBlank()) {
            return;
        }
        if (Character.isLetterOrDigit(value.charAt(0))) {
            return;
        }
        if (value.equals("?")) {
            return;
        }
        if (value.startsWith(":")) {
            char[] charArray = value.toCharArray();
            int count = 0;
            for (char c : charArray) {
                if (c == ':') {
                    count += 1;
                }
            }
            if (count == 1) {
                return;
            }
            if (count > 1) {
                throw new IllegalArgumentException(":는 하나여야 함");
            }
        }
        throw new IllegalArgumentException(":로 시작해야 함 = " + value);
    }

    public void setNamedParameter(String value) {
        if (value.startsWith(":")) {
            this.namedParameter = value.replace(":", "");
        }
    }

    public boolean isNamedParameterEmpty() {
        return this.namedParameter == null || this.namedParameter.isBlank();
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setValue(String name, Object value) {
        if (isNamedParameterEmpty()) {
            return;
        }
        if (this.namedParameter.equals(name)) {
            this.value = value;
        }
    }
}
