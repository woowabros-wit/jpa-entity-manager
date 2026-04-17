package entitymanager;

import executor.QueryExecutor;
import java.sql.SQLException;

public class IdentityAllocator {

    private final QueryExecutor queryExecutor;
    private final PersistenceContext persistenceContext;

    public IdentityAllocator(QueryExecutor queryExecutor, PersistenceContext persistenceContext) {
        this.queryExecutor = queryExecutor;
        this.persistenceContext = persistenceContext;
    }

    public long allocateNextId(Class<?> entityClass, String tableName, String idColumnName) throws SQLException {
        long nextAfterPersistedRows = queryExecutor.maxPrimaryKeyPlusOne(tableName, idColumnName);
        long maxInContext = persistenceContext.maxAssignedId(entityClass);
        return Math.max(nextAfterPersistedRows, maxInContext + 1);
    }
}
