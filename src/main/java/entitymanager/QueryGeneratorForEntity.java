package entitymanager;

import static entitymanager.EntityUtils.extractIdFieldName;
import static entitymanager.EntityUtils.extractIdFieldValue;
import static entitymanager.EntityUtils.extractTableName;

import builder.DeleteQueryBuilder;
import builder.InsertQueryBuilder;
import builder.Query;
import builder.UpdateQueryBuilder;
import builder.where.ComparisonCondition;
import builder.where.ComparisonOperator;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class QueryGeneratorForEntity {

    public <T> Query insert(T entity) {
        return new InsertQueryBuilder()
            .into(extractTableName(entity.getClass()))
            .value(extractIdFieldName(entity.getClass()), extractIdFieldValue(entity).toString())
            .values(generateColumnValues(entity));
    }

    public <T> Query update(T entity) {
        return new UpdateQueryBuilder()
            .table(extractTableName(entity.getClass()))
            .set(generateColumnValues(entity))
            .where(generateIdCondition(entity));
    }

    public <T> Query delete(T entity) {
        return new DeleteQueryBuilder()
            .from(extractTableName(entity.getClass()))
            .where(generateIdCondition(entity));
    }

    @NotNull
    private static <T> Map<String, String> generateColumnValues(T entity) {
        return Arrays.stream(entity.getClass().getDeclaredFields())
            .filter(field -> !field.isAnnotationPresent(Id.class))
            .map(field -> {
                try {
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    if (value == null) return null;
                    String sqlValue = value instanceof Number ? value.toString() : "'" + value + "'";
                    return Map.entry(field.getName(), sqlValue);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("엔티티의 필드 값을 추출하는데 실패하였습니다.", e);
                } finally {
                    field.setAccessible(false);
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private <T> ComparisonCondition generateIdCondition(T entity) {
        return new ComparisonCondition(
            extractIdFieldName(entity.getClass()),
            ComparisonOperator.EQ,
            extractIdFieldValue(entity).toString()
        );
    }
}
