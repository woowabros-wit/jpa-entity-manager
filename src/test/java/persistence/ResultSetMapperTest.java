package persistence;

import database.DatabaseServer;
import database.H2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResultSetMapperTest {

    private static Connection conn;

    @BeforeAll
    static void beforeAll() throws SQLException {
        final DatabaseServer server = new H2();
        server.start();
        conn = server.getConnection();

        // create user table
        conn.createStatement().execute("CREATE TABLE users (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "name VARCHAR(255) NOT NULL," +
                "age INT NOT NULL," +
                "email VARCHAR(255) NOT NULL" +
                ")");
    }

    private static void insert(String sql) throws SQLException {
        conn.createStatement().execute(sql);
    }

    @DisplayName("User 매핑 테스트")
    @Test
    void case1() throws Exception {
        insert("INSERT INTO users (name, age, email) VALUES ('zkdlu', 30, 'zkdlu@woowahan.com')");

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