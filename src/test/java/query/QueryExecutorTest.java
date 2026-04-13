package query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.entity.User;
import persistence.query.NamedParameterQuery;
import persistence.query.QueryExecutor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QueryExecutorTest {

    private Connection conn;
    private QueryExecutor executor;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");
        executor = new QueryExecutor(conn);

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS users");
            stmt.execute("""
                    CREATE TABLE users (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100),
                        age INT,
                        email VARCHAR(100)
                    )
                    """);

            stmt.execute("INSERT INTO users (name, age, email) VALUES ('John', 25, 'john@example.com')");
            stmt.execute("INSERT INTO users (name, age, email) VALUES ('Jane', 30, 'jane@example.com')");
            stmt.execute("INSERT INTO users (name, age, email) VALUES ('Bob', 18, 'bob@example.com')");
        }
    }

    @Test
    void query_일반_파라미터로_리스트_조회() throws Exception {
        String sql = "SELECT * FROM users WHERE age >= ?";
        List<User> users = executor.query(sql, User.class, 20);

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getName()).isEqualTo("John");
        assertThat(users.get(1).getName()).isEqualTo("Jane");
    }

    @Test
    void query_Named_Parameter로_리스트_조회() throws Exception {
        NamedParameterQuery query = new NamedParameterQuery(
                "SELECT * FROM users WHERE age > :minAge AND age < :maxAge"
        );
        query.setParameter("minAge", 20);
        query.setParameter("maxAge", 35);

        List<User> users = executor.query(query, User.class);

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getName()).isEqualTo("John");
    }
}