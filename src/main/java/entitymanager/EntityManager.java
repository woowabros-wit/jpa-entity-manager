package entitymanager;


import static entitymanager.EntityUtils.extractIdFieldName;
import static entitymanager.EntityUtils.extractIdFieldValue;
import static entitymanager.EntityUtils.extractTableName;

import builder.Query;
import builder.SelectQueryBuilder;
import builder.where.ComparisonCondition;
import builder.where.ComparisonOperator;
import executor.QueryExecutor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public class EntityManager {

    private Connection connection;
    private Transaction transaction;
    private PersistenceContext persistenceContext;
    private QueryExecutor queryExecutor;
    private QueryGeneratorForEntity queryGenerator;
    private IdentityAllocator identityAllocator;

    public EntityManager(Connection connection) {
        this.connection = connection;
        this.transaction = new Transaction(connection);
        this.persistenceContext = new PersistenceContext();
        this.queryExecutor = new QueryExecutor(connection);
        this.queryGenerator = new QueryGeneratorForEntity();
        this.identityAllocator = new IdentityAllocator(queryExecutor, persistenceContext);
    }

    public void close() {
        persistenceContext.clear();
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
        return result;
    }

    @Nullable
    private <T> T findById(Class<T> clazz, long id) {
        Query sql = new SelectQueryBuilder()
            .from(extractTableName(clazz))
            .where(new ComparisonCondition(extractIdFieldName(clazz), ComparisonOperator.EQ, String.valueOf(id)));

        T result = null;

        try {
            List<T> queryResult = queryExecutor.query(sql, clazz);
            result = queryResult.isEmpty() ? null : queryResult.getFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (result != null) {
            persistenceContext.save(clazz, EntityForPersistence.updateOf(result));
        }

        return result;
    }

    // TODO persist할때마다 id 조회 중이라 개선 필요
    public <T> void persist(T entity) {
        try {
            String tableName = extractTableName(entity.getClass());
            String idFieldName = extractIdFieldName(entity.getClass());
            Field idField = entity.getClass().getDeclaredField(idFieldName);

            idField.setAccessible(true);
            try {
                if (idField.get(entity) == null) {
                    long id = identityAllocator.allocateNextId(entity.getClass(), tableName, idFieldName);
                    idField.set(entity, id);
                    persistenceContext.save(entity.getClass(), EntityForPersistence.createOf(entity));
                } else {
                    persistenceContext.save(entity.getClass(), EntityForPersistence.updateOf(entity));
                }
            } finally {
                idField.setAccessible(false);
            }
        } catch (Exception e) {
            throw new IllegalStateException("엔티티 영속화에 실패했습니다.", e);
        }
    }

    public <T> void remove(T entity) {
        Long id = extractIdFieldValue(entity);
        EntityForPersistence persisted = persistenceContext.findEntityForPersistence(entity.getClass(), id);

        if (persisted != null && persisted.getStatus() == EntityForPersistence.Status.CREATED) {
            persistenceContext.remove(entity.getClass(), id);
            return;
        }

        if (persisted == null) {
            Object found = findById(entity.getClass(), id);
            if (found == null) {
                throw new IllegalArgumentException("삭제할 엔티티가 존재하지 않습니다. id=" + id);
            }
            entity = (T) found;
        }

        persistenceContext.save(entity.getClass(), EntityForPersistence.deleteOf(entity));
    }

    public void flush() {
        List<Query> queries = new ArrayList<>();
        List<Runnable> postFlushActions = new ArrayList<>();

        for (Map.Entry<Class<?>, Map<Long, EntityForPersistence>> entitiesWithClazz : persistenceContext.getAllEntities().entrySet()) {
            Class<?> clazz = entitiesWithClazz.getKey();
            Set<Map.Entry<Long, EntityForPersistence>> entitiesWithIds = entitiesWithClazz.getValue().entrySet();

            for (Map.Entry<Long, EntityForPersistence> entityWithId : entitiesWithIds) {
                Long id = entityWithId.getKey();
                EntityForPersistence entity = entityWithId.getValue();

                switch (entity.getStatus()) {
                    case CREATED -> {
                        queries.add(queryGenerator.insert(entity.getEntity()));
                        postFlushActions.add(entity::markFlushed);
                    }
                    case UPDATED -> {
                        if (entity.isDirty()) {
                            queries.add(queryGenerator.update(entity.getEntity()));
                        }
                        postFlushActions.add(entity::markFlushed);
                    }
                    case DELETED -> {
                        queries.add(queryGenerator.delete(entity.getEntity()));
                        postFlushActions.add(() -> persistenceContext.remove(clazz, id));
                    }
                }
            }
        }

        try {
            queryExecutor.execute(queries);
        } catch (SQLException e) {
            throw new IllegalStateException("flush에 실패했습니다.", e);
        }

        postFlushActions.forEach(Runnable::run);
    }
}
