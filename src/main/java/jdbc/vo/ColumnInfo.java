package jdbc.vo;

public class ColumnInfo {

    private final Class<?> clazz;
    private final Object value;

    public ColumnInfo(Class<?> clazz, Object value) {
        this.clazz = clazz;
        this.value = value;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Object getValue() {
        return value;
    }
}
