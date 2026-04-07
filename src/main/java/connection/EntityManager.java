package connection;

import jakarta.persistence.PersistenceContext;

import java.sql.Connection;
import java.sql.SQLException;

@PersistenceContext
public class EntityManager {

    private final Connection connection;

    public EntityManager(Connection connection) {
        this.connection = connection;
    }

    public void close() throws SQLException {
        this.connection.close();
    }

    public PersistenceContext getPersistenceContext() {
        return this.getClass().getAnnotation(PersistenceContext.class);
    }

    public Transaction getTransaction() {
        return new Transaction(connection);
    }
}
