package persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class ResultSetMapperTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
        conn.setAutoCommit(false);

        conn.createStatement().execute("CREATE TABLE users (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), age INT, email VARCHAR(255))");
        conn.createStatement().execute("INSERT INTO users (name, age, email) VALUES ('Soob', 30, 'test@email.com')");
    }

    @Test
    void ResultSet을_User객체로_매핑() throws Exception {
        String sql = "SELECT id, name, age, email FROM users WHERE age >= ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, 20);

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMapper mapper = new ResultSetMapper();
                List<User> users = mapper.mapToList(rs, User.class);

                for (User user : users) {
                    assertThat(user.getId()).isEqualTo(1L);
                    assertThat(user.getName()).isEqualTo("Soob");
                    assertThat(user.getAge()).isEqualTo(30);
                    assertThat(user.getEmail()).isEqualTo("test@email.com");

                }
            }
        }
    }

    @Test
    void JDBC타입이_Java타입으로_자동변환() throws Exception {

        String sql = "SELECT id, name, age, email FROM users WHERE age >= ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, 20);

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMapper mapper = new ResultSetMapper();
                List<User> users = mapper.mapToList(rs, User.class);

                for (User user : users) {
                    assertThat(user.getId()).isInstanceOf(Long.class);
                    assertThat(user.getName()).isInstanceOf(String.class);
                    assertThat(user.getAge()).isInstanceOf(Integer.class);
                    assertThat(user.getEmail()).isInstanceOf(String.class);
                }
            }
        }
    }
}