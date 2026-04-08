package persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class QueryExecutorTest {

    private final QueryExecutor queryExecutor = new QueryExecutor(
            DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "")
    );

    QueryExecutorTest() throws SQLException {
    }

    @BeforeEach
    void setUp() throws SQLException {
        queryExecutor.execute("drop table if exists users");
        queryExecutor.execute("CREATE TABLE users (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), age INT)");

    }

    @Test
    void execute() throws Exception {
        queryExecutor.execute(
                "INSERT INTO users (name, age) VALUES (?, ?)",
                "Alice",
                30
        );
        assertThat(
                queryExecutor.query("SELECT * FROM users WHERE name = ?", User.class, "Alice")
        ).extracting(User::getName)
                .containsExactly("Alice");

        assertThat(
                queryExecutor.query(new NamedParameterQuery("SELECT * FROM users WHERE name = :name").setParameter("name", "Alice"), User.class)
        ).extracting(User::getName)
                .containsExactly("Alice");
    }

    @Test
    void NamedParameterExecute() throws Exception {
        queryExecutor.execute(
                new NamedParameterQuery("INSERT INTO users (name, age) VALUES (:name, :age)")
                        .setParameter("name", "Bob")
                        .setParameter("age", 25)
        );
        assertThat(
                queryExecutor.query("SELECT * FROM users WHERE name = ?", User.class, "Bob")
        ).extracting(User::getName)
                .containsExactly("Bob");
    }
}