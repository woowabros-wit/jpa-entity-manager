package persistence;


import java.sql.Connection;

public class EntityManager extends SimpleEntityManager {
    private Transaction transaction;

    public EntityManager(Connection connection) {
        super(connection);
    }


    public PersistenceContext getPersistenceContext() {
        return new PersistenceContext();
    }

    public Transaction getTransaction() {
        if (transaction == null) {
            transaction = createTransaction();
            return transaction;
        }
        return transaction;
    }

    private Transaction createTransaction() {
        return new Transaction(getConnection());
    }


}
