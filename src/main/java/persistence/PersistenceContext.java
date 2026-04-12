package persistence;

import java.util.LinkedHashMap;
import java.util.Map;

public class PersistenceContext {

    private Map<Class<?>, Map<Long, Object>> entities = new LinkedHashMap<>();

    <T> T find(Class<T> clazz, long id) {
        return (T) entities.getOrDefault(clazz, new LinkedHashMap<>()).getOrDefault(id,null);
    }

    public <T> void add(T result, long id) {
        entities.computeIfAbsent(result.getClass(), k -> new LinkedHashMap<>())
                .put(id, result);
    }


}
