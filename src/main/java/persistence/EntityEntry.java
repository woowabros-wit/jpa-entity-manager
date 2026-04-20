package persistence;

import util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EntityEntry {

    private final Object entity;
    private final Field[] entityFields;
    private final Map<Field, Object> oldValueByField;

    public EntityEntry(Object entity) {
        this.entity = Objects.requireNonNull(entity, "entity 는 필수 입니다.");
        this.entityFields = entity.getClass().getDeclaredFields();
        this.oldValueByField = extractOldValues(entity);
    }

    private Map<Field, Object> extractOldValues(Object entity) {
        return Arrays.stream(entityFields)
                .collect(Collectors.toUnmodifiableMap(Function.identity(), field -> ReflectionUtils.getValue(field, entity)));
    }

    public Object getEntity() {
        return entity;
    }

    public boolean isModified() {
        return Arrays.stream(entityFields)
                .anyMatch(entityField -> {
                    final Object oldValue = oldValueByField.get(entityField);
                    final Object newValue = ReflectionUtils.getValue(entityField, entity);
                    return !Objects.equals(oldValue, newValue);
                });
    }

}
