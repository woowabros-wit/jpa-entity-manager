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

    public void close() throws SQLException {
        connection.close();
    }
}
