package entitymanager;

import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public class EntityUtils {

    public static String extractTableName(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalStateException(clazz.getSimpleName() + "은 엔티티 클래스가 아닙니다.");
        }
        return clazz.getAnnotation(Table.class).name();
    }

    @NotNull
    public static <T> String extractIdFieldName(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(Id.class))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("id 필드를 찾을 수 없습니다."))
            .getName();
    }

    public static <T> Long extractIdFieldValue(T entity) {
        String idFieldName = extractIdFieldName(entity.getClass());

        try {
            var field = entity.getClass().getDeclaredField(idFieldName);
            field.setAccessible(true);
            Long id = (Long) field.get(entity);
            field.setAccessible(false);
            return id;
        } catch (Exception e) {
            throw new IllegalStateException("id 값 추출에 실패하였습니다.");
        }
    }
}
