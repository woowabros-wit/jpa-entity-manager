package database;


import java.sql.Connection;

public class EntityManager {

    private Connection connection;
    private Transaction transaction;
    private PersistenceContext persistenceContext;

    public EntityManager(Connection connection) {
        this.connection = connection;
        this.transaction = new Transaction(connection);
        this.persistenceContext = new PersistenceContext();
    }

    public void close() {
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
}
