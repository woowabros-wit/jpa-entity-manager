package persistence;

import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NamedParameterQueryTest {
    @Test
    public void test() throws Exception {
        var conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
        conn.setAutoCommit(false);

        conn.createStatement().execute("CREATE TABLE users (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), " +
                "age INT, email VARCHAR(255))");
        conn.createStatement().execute("INSERT INTO users (name, age, email) VALUES ('Soob', 30, 'test@email.com')");

        String sql = "SELECT * FROM users WHERE age >= :minAge AND name LIKE :namePattern";

        NamedParameterQuery query = new NamedParameterQuery(sql);
        query.setParameter("minAge", 20);
        query.setParameter("namePattern", "%John%");

// JDBC SQL로 변환
        String jdbcSql = query.toJdbcSql();
// 결과: "SELECT * FROM users WHERE age >= ? AND name LIKE ?"

// PreparedStatement 생성 및 파라미터 바인딩
        try (PreparedStatement pstmt = conn.prepareStatement(jdbcSql)) {
            query.bindParameters(pstmt);  // 자동으로 파라미터 설정!

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
}