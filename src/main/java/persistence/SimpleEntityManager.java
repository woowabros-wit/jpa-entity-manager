package persistence;

import java.sql.Connection;
import java.sql.SQLException;

public class SimpleEntityManager {
    private final Connection connection;

    public SimpleEntityManager(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close connection", e);
        }
    }
}
