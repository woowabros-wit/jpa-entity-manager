import jakarta.persistence.EntityTransaction;
import persistence.PersistenceContext;

import java.sql.Connection;
import java.sql.SQLException;

public class EntityManager {

    private final Connection conn;
    private final PersistenceContext persistenceContext = new PersistenceContext();
    private final EntityTransaction transaction;

    public EntityManager(Connection conn) {
        this.conn = conn;
        this.transaction = new EntityTransactionImpl(conn);
    }

    public void close() throws SQLException {
        conn.close();
    }

    public PersistenceContext getPersistenceContext() {
        return persistenceContext;
    }

    public EntityTransaction getTransaction() {
        return transaction;
    }
}
