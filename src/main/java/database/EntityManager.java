package database;

import java.sql.Connection;

public class EntityManager {
    private Connection connection;

    public EntityManager(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close(){
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
