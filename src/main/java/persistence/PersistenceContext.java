package persistence;

public class PersistenceContext {

    private final PersistenceCacheManager cacheManager;

    public PersistenceContext() {
        this.cacheManager = new PersistenceCacheManager();
    }

    public <T> T get(Class<T> entityClass, Object key) {
        return cacheManager.get(entityClass, key);

    }

    public <T> void save(Class<T> entityClass, Object key, T entity) {
        cacheManager.save(entityClass, key, entity);
    }
}
