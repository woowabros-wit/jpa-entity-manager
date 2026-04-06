import jakarta.persistence.RollbackException;

import java.sql.Connection;
import java.sql.SQLException;

class EntityTransactionImpl implements jakarta.persistence.EntityTransaction {

    private final Connection connection;
    private boolean active;

    EntityTransactionImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void begin() {
        try {
            connection.setAutoCommit(false);
            active = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void commit() {
        try {
            connection.commit();
            connection.setAutoCommit(true);
            active = false;
        } catch (SQLException e) {
            throw new RollbackException(e.getMessage());
        }
    }

    @Override
    public void rollback() {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
            active = false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setRollbackOnly() {}

    @Override
    public boolean getRollbackOnly() {
        return false;
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
