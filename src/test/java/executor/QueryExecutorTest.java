package executor;

import static org.assertj.core.api.Assertions.assertThat;

import builder.DeleteQueryBuilder;
import builder.InsertQueryBuilder;
import builder.SelectQueryBuilder;
import builder.UpdateQueryBuilder;
import builder.where.ComparisonCondition;
import builder.where.ComparisonOperator;
import entity.User;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
class QueryExecutorTest {

    private Connection connection;
    private QueryExecutor executor;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "SA", "");
        executor = new QueryExecutor(connection);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                + "name VARCHAR(255), "
                + "age INT, "
                + "email VARCHAR(255))");
            stmt.execute("INSERT INTO users (name, age, email) VALUES ('Alice', 25, 'alice@test.com')");
            stmt.execute("INSERT INTO users (name, age, email) VALUES ('Bob', 30, 'bob@test.com')");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE users");
        }
        connection.close();
    }

    @Test
    void SELECT_전체_조회() throws Exception {
        var query = new SelectQueryBuilder()
            .select("id", "name", "age", "email")
            .from("users");

        List<User> users = executor.query(query, User.class);

        assertThat(users).hasSize(2);
        assertThat(getField(users.get(0), "name")).isEqualTo("Alice");
        assertThat(getField(users.get(1), "name")).isEqualTo("Bob");
    }

    @Test
    void SELECT_빈_결과() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM users");
        }

        var query = new SelectQueryBuilder().from("users");

        List<User> users = executor.query(query, User.class);

        assertThat(users).isEmpty();
    }

    @Test
    void INSERT_실행() throws Exception {
        var query = new InsertQueryBuilder()
            .into("users")
            .value("name", "'Charlie'")
            .value("age", "35")
            .value("email", "'charlie@test.com'");

        int result = executor.execute(query);

        assertThat(result).isEqualTo(1);

        List<User> users = executor.query(
            new SelectQueryBuilder().from("users"), User.class);
        assertThat(users).hasSize(3);
    }

    @Test
    void UPDATE_실행() throws Exception {
        var query = new UpdateQueryBuilder()
            .table("users")
            .set("name", "'Updated'")
            .where(new ComparisonCondition("name", ComparisonOperator.EQ, "'Alice'"));

        int result = executor.execute(query);

        assertThat(result).isEqualTo(1);

        List<User> users = executor.query(
            new SelectQueryBuilder().select("name").from("users"), User.class);
        assertThat(getField(users.get(0), "name")).isEqualTo("Updated");
    }

    @Test
    void DELETE_실행() throws Exception {
        var query = new DeleteQueryBuilder()
            .from("users")
            .where(new ComparisonCondition("name", ComparisonOperator.EQ, "'Bob'"));

        int result = executor.execute(query);
        assertThat(result).isEqualTo(1);

        List<User> users = executor.query(new SelectQueryBuilder().from("users"), User.class);
        assertThat(users).hasSize(1);
        assertThat(getField(users.get(0), "name")).isEqualTo("Alice");
    }

    private Object getField(User user, String fieldName) throws Exception {
        Field field = User.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(user);
    }
}
