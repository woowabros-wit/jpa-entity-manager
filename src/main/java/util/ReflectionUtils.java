package util;

import java.lang.reflect.Field;

public class ReflectionUtils {

        public static Object getValue(Field field, Object target) {
            try {
                field.setAccessible(true);
                return field.get(target);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

    public static void setValue(Field field, Object target, Object value) {
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
