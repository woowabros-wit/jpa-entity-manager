package jdbc;

import database.H2;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultSetMapperTest {

    private static H2 h2;
    private Connection connection;
    private final ResultSetMapper mapper = new ResultSetMapper();

    @BeforeAll
    static void beforeAll() throws SQLException {
        h2 = new H2();
        h2.start();
    }

    @AfterAll
    static void afterAll() {
        h2.stop();
    }

    @BeforeEach
    void setUp() throws SQLException {
        connection = h2.getConnection();
        Statement stmt = connection.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS test_users (" +
                "id BIGINT PRIMARY KEY, " +
                "user_name VARCHAR(255), " +
                "age INT, " +
                "is_active BOOLEAN, " +
                "created_at TIMESTAMP" +
                ")");
        stmt.execute("DELETE FROM test_users");
        stmt.execute("INSERT INTO test_users (id, user_name, age, is_active, created_at) VALUES (1, 'kim', 25, true, '2025-01-15 10:30:00')");
        stmt.execute("INSERT INTO test_users (id, user_name, age, is_active, created_at) VALUES (2, 'lee', 30, false, '2025-02-20 14:00:00')");
        stmt.execute("INSERT INTO test_users (id, user_name, age, is_active, created_at) VALUES (3, 'park', 28, true, '2025-03-10 09:15:00')");
        stmt.close();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("단일_행을_객체로_매핑한다")
    void test01() throws Exception {
        PreparedStatement pstmt = connection.prepareStatement("SELECT id, user_name, age, is_active, created_at FROM test_users WHERE id = 1");
        ResultSet rs = pstmt.executeQuery();
        rs.next();

        TestUser user = mapper.mapToObject(rs, TestUser.class);

        assertEquals(1L, user.id);
        assertEquals("kim", user.userName);
        assertEquals(25, user.age);
        assertTrue(user.isActive);
        assertEquals(LocalDateTime.of(2025, 1, 15, 10, 30, 0), user.createdAt);

        rs.close();
        pstmt.close();
    }

    @Test
    @DisplayName("여러_행을_리스트로_매핑한다")
    void test02() throws Exception {
        PreparedStatement pstmt = connection.prepareStatement("SELECT id, user_name, age, is_active, created_at FROM test_users ORDER BY id");
        ResultSet rs = pstmt.executeQuery();

        List<TestUser> users = mapper.mapToList(rs, TestUser.class);

        assertEquals(3, users.size());
        assertEquals("kim", users.get(0).userName);
        assertEquals("lee", users.get(1).userName);
        assertEquals("park", users.get(2).userName);

        rs.close();
        pstmt.close();
    }

    @Test
    @DisplayName("snake_case_컬럼명이_camelCase_필드에_매핑된다")
    void test03() throws Exception {
        PreparedStatement pstmt = connection.prepareStatement("SELECT id, user_name, age, is_active, created_at FROM test_users WHERE id = 1");
        ResultSet rs = pstmt.executeQuery();
        rs.next();

        TestUser user = mapper.mapToObject(rs, TestUser.class);

        assertEquals("kim", user.userName);
        assertTrue(user.isActive);
        assertNotNull(user.createdAt);

        rs.close();
        pstmt.close();
    }

    @Test
    @DisplayName("결과가_없으면_빈_리스트를_반환한다")
    void test04() throws Exception {
        PreparedStatement pstmt = connection.prepareStatement("SELECT id, user_name, age, is_active, created_at FROM test_users WHERE id = 999");
        ResultSet rs = pstmt.executeQuery();

        List<TestUser> users = mapper.mapToList(rs, TestUser.class);

        assertTrue(users.isEmpty());

        rs.close();
        pstmt.close();
    }

    @Test
    @DisplayName("필드에_있지만_컬럼에_없으면_예외가_발생한다")
    void test05() throws Exception {
        PreparedStatement pstmt = connection.prepareStatement("SELECT id, user_name FROM test_users WHERE id = 1");
        ResultSet rs = pstmt.executeQuery();
        rs.next();

        assertThrows(Exception.class, () -> mapper.mapToObject(rs, TestUser.class));

        rs.close();
        pstmt.close();
    }

    @Test
    @DisplayName("필드_타입과_컬럼_타입이_다르면_예외가_발생한다")
    void test06() throws Exception {
        PreparedStatement pstmt = connection.prepareStatement("SELECT id, user_name, age, is_active, created_at FROM test_users WHERE id = 1");
        ResultSet rs = pstmt.executeQuery();
        rs.next();

        assertThrows(Exception.class, () -> mapper.mapToObject(rs, TypeMismatchUser.class));

        rs.close();
        pstmt.close();
    }

    @Test
    @DisplayName("컬럼이_필드보다_많아도_매핑에_성공한다")
    void test07() throws Exception {
        PreparedStatement pstmt = connection.prepareStatement("SELECT id, user_name, age, is_active, created_at FROM test_users WHERE id = 1");
        ResultSet rs = pstmt.executeQuery();
        rs.next();

        PartialUser user = mapper.mapToObject(rs, PartialUser.class);

        assertEquals(1L, user.id);
        assertEquals("kim", user.userName);

        rs.close();
        pstmt.close();
    }

    public static class TestUser {
        Long id;
        String userName;
        Integer age;
        Boolean isActive;
        LocalDateTime createdAt;

        public TestUser() {}
    }

    public static class TypeMismatchUser {
        String id;
        String userName;
        Integer age;
        Boolean isActive;
        LocalDateTime createdAt;

        public TypeMismatchUser() {}
    }

    public static class PartialUser {
        Long id;
        String userName;

        public PartialUser() {}
    }
}
