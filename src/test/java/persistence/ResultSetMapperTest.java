package persistence;

import entity.User;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResultSetMapperTest {

    private static Connection conn;

    @BeforeAll
    static void beforeAll() throws SQLException {
        conn = DriverManager.getConnection(
                "jdbc:h2:mem:simpleEntityManagerTest;DB_CLOSE_DELAY=-1", "sa", ""
        );

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(
                    "CREATE TABLE users (" +
                            "id INT PRIMARY KEY AUTO_INCREMENT," +
                            "name VARCHAR(255) NOT NULL," +
                            "age INT NOT NULL," +
                            "email VARCHAR(255) NOT NULL" +
                            ")"
            );
        }
    }


    @AfterAll
    static void teardownDatabase() throws SQLException {
        if (conn != null) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE users");
            }

            conn.close();
        }
    }

    @BeforeEach
    void insertTestData() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM users");
            stmt.execute("INSERT INTO users (id, name, age, email) VALUES (1, 'zkdlu', 30, 'zkdlu@woowahan.com')");
        }
    }


    @DisplayName("User 매핑 테스트")
    @Test
    void case1() throws Exception {
        String sql = "SELECT id, name, age, email FROM users WHERE age >= ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, 20);

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMapper mapper = new ResultSetMapper();
                List<User> users = mapper.mapToList(rs, User.class);

                assertThat(users).hasSize(1);
                assertThat(users.get(0).getName()).isEqualTo("zkdlu");
                assertThat(users.get(0).getAge()).isEqualTo(30);
                assertThat(users.get(0).getEmail()).isEqualTo("zkdlu@woowahan.com");
            }
        }
    }
}