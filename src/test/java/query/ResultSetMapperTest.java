package query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.entity.User;
import persistence.query.ResultSetMapper;

import java.sql.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResultSetMapperTest {

    private Connection conn;
    private ResultSetMapper mapper;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");
        mapper = new ResultSetMapper();

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS users");
            stmt.execute("""
                    CREATE TABLE users (
                        id BIGINT PRIMARY KEY,
                        name VARCHAR(100),
                        age INT,
                        email VARCHAR(100)
                    )
                    """);

            stmt.execute("INSERT INTO users (id, name, age, email) VALUES (1, 'John', 25, 'john@example.com')");
            stmt.execute("INSERT INTO users (id, name, age, email) VALUES (2, 'Jane', 30, 'jane@example.com')");
            stmt.execute("INSERT INTO users (id, name, age, email) VALUES (3, 'Bob', 18, 'bob@example.com')");
        }
    }

    @Test
    void mapToList_자동_매핑_테스트() throws Exception {
        String sql = "SELECT id, name, age, email FROM users WHERE age >= ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, 20);

            try (ResultSet rs = pstmt.executeQuery()) {
                List<User> users = mapper.mapToList(rs, User.class);

                assertThat(users.size()).isEqualTo(2);

                User john = users.get(0);
                assertThat(john.getId()).isEqualTo(1L);
                assertThat(john.getName()).isEqualTo("John");
                assertThat(john.getAge()).isEqualTo(25);
                assertThat(john.getEmail()).isEqualTo("john@example.com");

                User jane = users.get(1);
                assertThat(jane.getId()).isEqualTo(2L);
                assertThat(jane.getName()).isEqualTo("Jane");
                assertThat(jane.getAge()).isEqualTo(30);
                assertThat(jane.getEmail()).isEqualTo("jane@example.com");
            }
        }
    }
}