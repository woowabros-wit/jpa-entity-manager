package persistence;

import entity.User;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NamedParameterQueryTest {

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
                            "email VARCHAR(255) NOT NULL," +
                            "height INT NOT NULL" +
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
            stmt.execute("INSERT INTO users (id, name, age, email, height) VALUES (1, 'zkdlu', 30, 'zkdlu@woowahan.com', 200)");
        }
    }

    @DisplayName("Named Parameter 쿼리 변환 테스트")
    @Test
    void case1() throws SQLException {
        // Named Parameter 사용
        String sql = "SELECT * FROM users WHERE age >= :minAge AND name LIKE :namePattern";

        NamedParameterQuery query = new NamedParameterQuery(sql);
        query.setParameter("minAge", 20);
        query.setParameter("namePattern", "%John%");

        // JDBC SQL로 변환
        String jdbcSql = query.toJdbcSql();

        assertThat(jdbcSql).isEqualTo("SELECT * FROM users WHERE age >= ? AND name LIKE ?");
    }

    private static void insert(String sql) throws SQLException {
        conn.createStatement().execute(sql);
    }

    @DisplayName("Named Parameter 쿼리 실행 테스트")
    @Test
    void case2() throws Exception {


        String sql = "SELECT * FROM users WHERE age >= :minAge AND name LIKE :namePattern";

        NamedParameterQuery query = new NamedParameterQuery(sql);
        query.setParameter("minAge", 20);
        query.setParameter("namePattern", "%zkdlu%");

        String jdbcSql = query.toJdbcSql();

        // PreparedStatement 생성 및 파라미터 바인딩
        try (PreparedStatement pstmt = conn.prepareStatement(jdbcSql)) {
            query.bindParameters(pstmt);  // 자동으로 파라미터 설정!

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

    @DisplayName("Named Parameter 쿼리 변환 테스트 - 2")
    @Test
    void case3() throws Exception {
        String sql = "SELECT * FROM users WHERE age >= :min AND height >= :min";

        NamedParameterQuery query = new NamedParameterQuery(sql);
        query.setParameter("min", 20);

        String jdbcSql = query.toJdbcSql();

        assertThat(jdbcSql).isEqualTo("SELECT * FROM users WHERE age >= ? AND height >= ?");
    }


    @DisplayName("Named Parameter 쿼리 실행 테스트 - 2")
    @Test
    void case4() throws Exception {
        String sql = "SELECT * FROM users WHERE age >= :min AND height >= :min";

        NamedParameterQuery query = new NamedParameterQuery(sql);
        query.setParameter("min", 20);

        String jdbcSql = query.toJdbcSql();

        // PreparedStatement 생성 및 파라미터 바인딩
        try (PreparedStatement pstmt = conn.prepareStatement(jdbcSql)) {
            query.bindParameters(pstmt);  // 자동으로 파라미터 설정!

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