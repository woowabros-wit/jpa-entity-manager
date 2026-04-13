package persistence.query;

import java.util.Arrays;

public enum DirectionType {
    ASC, DESC,
    ;

    public static DirectionType from(String direction) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(direction))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid direction: " + direction));
    }
}
