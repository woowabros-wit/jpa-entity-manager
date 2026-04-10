package entitymanager;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PersistenceContext {

    private Map<Class<?>, Map<Long, Object>> entities = new HashMap<>();

    public <T> void save(Class<T> clazz, Object entity) {
        if (entity == null) {
            return;
        }

        Long id = findEntityId(clazz, entity);

        Map<Long, Object> entitiesByClazz = entities.getOrDefault(clazz, new HashMap<>());
        entitiesByClazz.put(id, entity);

        entities.put(clazz, entitiesByClazz);
    }

    private <T> Long findEntityId(Class<T> clazz, Object entity) {
        String idFieldName = Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(Id.class))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("id 필드를 찾을 수 없습니다."))
            .getName();
        try {
            var field = clazz.getDeclaredField(idFieldName);
            field.setAccessible(true);
            Long id = (Long) field.get(entity);
            field.setAccessible(false);
            return id;
        } catch (Exception e) {
            throw new IllegalStateException("id 값 추출에 실패하였습니다.");
        }
    }

    public <T> T find(Class<T> clazz, long id) {
        return (T) entities.getOrDefault(clazz, Collections.emptyMap()).getOrDefault(id, null);
    }
}
