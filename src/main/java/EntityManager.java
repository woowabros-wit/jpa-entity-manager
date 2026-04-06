import jakarta.persistence.EntityTransaction;

import java.sql.Connection;
import java.sql.SQLException;

class EntityManager {

    private final Connection conn;
    private final Object persistenceContext = new Object();
    private final EntityTransaction transaction;

    public EntityManager(Connection conn) {
        this.conn = conn;
        this.transaction = new EntityTransactionImpl(conn);
    }

    public void close() throws SQLException {
        conn.close();
    }

    public Object getPersistenceContext() {
        return persistenceContext;
    }

    public EntityTransaction getTransaction() {
        return transaction;
    }
}
