package entitymanager;

import java.sql.Connection;
import java.sql.SQLException;

public class EntityManager {
    private final Connection connection;
    private final PersistenceContext persistenceContext;
    private final EntityTransaction transaction;

    public EntityManager(Connection connection) {
        this.connection = connection;
        this.persistenceContext = new PersistenceContext();
        this.transaction = new EntityTransaction(connection);
    }

    public PersistenceContext getPersistenceContext() {
        return persistenceContext;
    }

    public EntityTransaction getTransaction() {
        return transaction;
    }

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
}
