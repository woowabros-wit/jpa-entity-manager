package connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import builder.SelectQueryBuilder;
import database.EntityManager;
import entity.User;
import executor.QueryExecutor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Test;


class ConnectionOwnershipTest {

    @Test
    void Connection은_EntityManager가_소유한다() throws SQLException {
        Connection connection = DriverManager.getConnection(
            "jdbc:h2:mem:test", "sa", ""
        );
        assertFalse(connection.isClosed());

        // When: EntityManager에게 소유권 이전
        EntityManager em = new EntityManager(connection);

        // Then: EntityManager가 관리 중
        assertNotNull(em.getConnection());
        assertFalse(em.getConnection().isClosed());

        // When: EntityManager 종료
        em.close();

        // Then: Connection도 같이 닫힘
        assertTrue(connection.isClosed());
    }

    @Test
    void 하위_컴포넌트는_Connection을_닫지_않는다() throws Exception {
        // Given
        Connection connection = DriverManager.getConnection(
            "jdbc:h2:mem:test", "sa", ""
        );
        ddl(connection, "CREATE TABLE users (id BIGINT, name VARCHAR(255), age INT)");

        // When: 하위 컴포넌트가 Connection 사용
        QueryExecutor executor = new QueryExecutor(connection);
        executor.query(
            new SelectQueryBuilder()
                .from("users")
                .limit(1),
            User.class
        );

        // Then: 하위 컴포넌트는 Connection을 닫지 않음
        assertFalse(connection.isClosed());

        // Cleanup
        ddl(connection, "DROP TABLE users");
        connection.close();
    }

    private void ddl(Connection connection, String ddl) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(ddl);
        }
    }
}
