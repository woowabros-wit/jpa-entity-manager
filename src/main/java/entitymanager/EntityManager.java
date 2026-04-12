package entitymanager;


import builder.Query;
import builder.SelectQueryBuilder;
import builder.where.ComparisonCondition;
import builder.where.ComparisonOperator;
import executor.QueryExecutor;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityManager {

    private Connection connection;
    private Transaction transaction;
    private PersistenceContext persistenceContext;
    private QueryExecutor queryExecutor;

    public EntityManager(Connection connection) {
        this.connection = connection;
        this.transaction = new Transaction(connection);
        this.persistenceContext = new PersistenceContext();
        this.queryExecutor = new QueryExecutor(connection);
    }

    public void close() {
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public PersistenceContext getPersistenceContext() {
        return persistenceContext;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public <T> T find(Class<T> clazz, long id) {
        var persistenceData = persistenceContext.find(clazz, id);
        if (persistenceData != null) {
            return persistenceData;
        }

        T result = findById(clazz, id);
        persistenceContext.save(clazz, result);
        return result;
    }

    @Nullable
    private <T> T findById(Class<T> clazz, long id) {
        Query sql = new SelectQueryBuilder()
            .from(clazz.getAnnotation(Table.class).name())
            .where(new ComparisonCondition(getIdFieldName(clazz), ComparisonOperator.EQ, String.valueOf(id)));

        try {
            List<T> queryResult = queryExecutor.query(sql, clazz);
            return queryResult.isEmpty() ? null : queryResult.getFirst();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @NotNull
    private static <T> String getIdFieldName(Class<T> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(Id.class))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("id 필드를 찾을 수 없습니다."))
            .getName();
    }
}
