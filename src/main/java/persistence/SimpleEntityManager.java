package persistence;

import util.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SimpleEntityManager implements AutoCloseable {

    private static final EntityMetaDataCache ENTITY_META_DATA_CACHE = new EntityMetaDataCache();

    private final List<Object> insertTargets = new ArrayList<>();
    private final PersistenceContext persistenceContext = new PersistenceContext();

    private final Connection connection;
    private final Transaction transaction;
    private final EntityLoader entityLoader;
    private final EntityPersister entityPersister;

    public SimpleEntityManager(Connection connection) {
        this.connection = connection;
        this.transaction = new Transaction(connection);
        this.entityLoader = new EntityLoader(connection, ENTITY_META_DATA_CACHE);
        this.entityPersister = new EntityPersister(connection, ENTITY_META_DATA_CACHE);
    }

    public Connection getConnection() {
        return connection;
    }

    public <T> T find(Class<T> entityClass, Object id) {
        final EntityKey entityKey = new EntityKey( entityClass, id);
        final Object cached = persistenceContext.get(entityKey);
        if (cached != null) {
            return entityClass.cast(cached);
        }

        final T entity = entityLoader.load(entityClass, id);
        if (entity == null) {
            return null;
        }
        persistenceContext.put(entityKey, entity);
        return entity;
    }

    @Override
    public void close() {
        try {
            if (connection.isClosed()) {
                return;
            }
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void persist(Object entity) {
        final Class<?> entityClass = entity.getClass();
        final Object idValue = extractIdValue(entity);
        if (idValue == null) {
            insertTargets.add(entity);
            return;
        }
        final Object cached = persistenceContext.get(new EntityKey(entityClass, idValue));
        if (cached != null) {
            return;
        }
        insertTargets.add(entity);
    }

    public void flush() {
        final List<Object> results = new ArrayList<>();
        for (Object target : insertTargets) {
            final Object result = entityPersister.insert(target);
            results.add(result);
        }
        insertTargets.clear();

        for (Object modifiedEntity : persistenceContext.getModifiedEntities()) {
            entityPersister.update(modifiedEntity);
            results.add(modifiedEntity);
        }

        for (Object result : results) {
            persistenceContext.put(new EntityKey(result.getClass(), extractIdValue(result)), result);
        }
    }

    private Object extractIdValue(Object target) {
        final EntityMetaData entityMetaData = ENTITY_META_DATA_CACHE.get(target);
        final Field idField = entityMetaData.getIdField();
        return ReflectionUtils.getValue(idField, target);
    }

}
