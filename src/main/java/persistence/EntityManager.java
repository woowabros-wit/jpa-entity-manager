package persistence;


import java.sql.Connection;
import java.util.List;
import java.util.Objects;

public class EntityManager extends SimpleEntityManager {
    private Transaction transaction;
    private PersistenceContext persistenceContext;
    private final QueryExecutor queryExecutor;

    public EntityManager(Connection connection) {
        super(connection);
        this.transaction = new Transaction(connection);
        this.persistenceContext = new PersistenceContext();
        this.queryExecutor = new QueryExecutor(connection);
    }


    public PersistenceContext getPersistenceContext() {
        if (persistenceContext == null) {
            persistenceContext = createPersistenceContext();
            return persistenceContext;
        }
        return persistenceContext;
    }

    private PersistenceContext createPersistenceContext() {
        return new PersistenceContext();
    }

    public Transaction getTransaction() {
        if (transaction == null) {
            transaction = createTransaction();
            return transaction;
        }
        return transaction;
    }

    private Transaction createTransaction() {
        return new Transaction(getConnection());
    }


    public <T> T find(Class<T> clazz, long l) throws Exception {
        if (persistenceContext.find(clazz, l) != null) {
            return persistenceContext.find(clazz, l);
        }
        var result = findById(clazz, l);
        persistenceContext.add(Objects.requireNonNull(result), l);
        return result;
    }

    private <T> T findById(Class<T> clazz, long id) throws Exception {
        String sql = new SelectQueryBuilder()
                .from(clazz.getAnnotation(Table.class).name())
                .where(getIdFieldName(clazz) + " = " + id)
                .build();

        List<T> queryResult = queryExecutor.query(sql, clazz);
        return queryResult.isEmpty() ? null : queryResult.getFirst();
    }

    private <T> String getIdFieldName(Class<T> clazz) {
        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field.getName();
            }
        }
        throw new IllegalArgumentException("No @Id field found in " + clazz.getName());
    }
}
