package connection;

import jakarta.persistence.PersistenceContext;

import java.sql.Connection;
import java.sql.SQLException;

@PersistenceContext
public class EntityManager {

    private final Connection connection;
    private Transaction transaction;

    public EntityManager(Connection connection) {
        this.connection = connection;
        this.transaction = new Transaction(this.connection);
    }

    public void close() throws SQLException {
        this.connection.close();
    }

    public PersistenceContext getPersistenceContext() {
        return this.getClass().getAnnotation(PersistenceContext.class);
    }

    public Transaction getTransaction() {
        return this.transaction;
    }


}
