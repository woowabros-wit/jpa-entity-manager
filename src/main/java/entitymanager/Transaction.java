package entitymanager;

import java.sql.Connection;

public class Transaction {

    private Connection connection;

    public Transaction(Connection connection) {
        this.connection = connection;
    }

    public void begin() {
        try {
            connection.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commit() {
        try {
            connection.setAutoCommit(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
