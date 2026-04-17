package entitymanager;

import java.sql.Connection;
import java.sql.SQLException;

public class Transaction {

    private Connection connection;

    public Transaction(Connection connection) {
        this.connection = connection;
    }

    public void begin() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new IllegalStateException("트랜잭션 시작에 실패했습니다.", e);
        }
    }

    public void commit() {
        try {
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IllegalStateException("트랜잭션 커밋에 실패했습니다.", e);
        }
    }

    public void rollback() {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IllegalStateException("트랜잭션 롤백에 실패했습니다.", e);
        }
    }
}
