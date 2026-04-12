package persistence;

import java.sql.Connection;
import java.sql.SQLException;

public class Transaction {
    private final Connection connection;

    public Transaction(Connection connection) {
        this.connection = connection;
    }

    public void begin() throws SQLException {
        System.out.println("Transaction started.");
        connection.setAutoCommit(false);
    }

    public void commit() throws SQLException {
        System.out.println("Transaction committed.");
        connection.setAutoCommit(true);
    }
}
