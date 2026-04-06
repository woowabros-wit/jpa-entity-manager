package persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class QueryExecutorTest {

    private Connection connection;
    private PreparedStatement pstmt;
    private ResultSet rs;
    private ResultSetMetaData metaData;
    private QueryExecutor executor;

    @BeforeEach
    void setUp() throws SQLException {
        connection = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);
        metaData = mock(ResultSetMetaData.class);
        executor = new QueryExecutor(connection);

        when(rs.getMetaData()).thenReturn(metaData);
    }

    @Test
    void SELECT_쿼리를_실행하고_결과를_매핑한다() throws Exception {
        String sql = "SELECT id, name FROM users WHERE age >= ?";
        when(connection.prepareStatement(sql)).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(1)).thenReturn("id");
        when(metaData.getColumnName(2)).thenReturn("name");
        when(rs.getObject(1)).thenReturn(1L);
        when(rs.getObject(2)).thenReturn("John");

        var users = executor.query(sql, TestUser.class, 20);

        assertThat(users).hasSize(1);
        assertThat(users.get(0).id).isEqualTo(1L);
        assertThat(users.get(0).name).isEqualTo("John");
        verify(pstmt).setObject(1, 20);
    }

    @Test
    void NamedParameterQuery로_SELECT를_실행한다() throws Exception {
        NamedParameterQuery query = new NamedParameterQuery(
                "SELECT id, name FROM users WHERE age >= :minAge");
        query.setParameter("minAge", 20);

        when(connection.prepareStatement("SELECT id, name FROM users WHERE age >= ?")).thenReturn(pstmt);
        when(pstmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(1)).thenReturn("id");
        when(metaData.getColumnName(2)).thenReturn("name");
        when(rs.getObject(1)).thenReturn(1L);
        when(rs.getObject(2)).thenReturn("Alice");

        var users = executor.query(query, TestUser.class);

        assertThat(users).hasSize(1);
        assertThat(users.get(0).name).isEqualTo("Alice");
        verify(pstmt).setObject(1, 20);
    }

    @Test
    void INSERT_쿼리를_실행한다() throws Exception {
        String sql = "INSERT INTO users (name, age) VALUES (?, ?)";
        when(connection.prepareStatement(sql)).thenReturn(pstmt);
        when(pstmt.executeUpdate()).thenReturn(1);

        int result = executor.execute(sql, "John", 30);

        assertThat(result).isEqualTo(1);
        verify(pstmt).setObject(1, "John");
        verify(pstmt).setObject(2, 30);
    }

    @Test
    void NamedParameterQuery로_UPDATE를_실행한다() throws Exception {
        NamedParameterQuery query = new NamedParameterQuery(
                "UPDATE users SET name = :name WHERE id = :id");
        query.setParameter("name", "Alice");
        query.setParameter("id", 1);

        when(connection.prepareStatement("UPDATE users SET name = ? WHERE id = ?")).thenReturn(pstmt);
        when(pstmt.executeUpdate()).thenReturn(1);

        int result = executor.execute(query);

        assertThat(result).isEqualTo(1);
        verify(pstmt).setObject(1, "Alice");
        verify(pstmt).setObject(2, 1);
    }

    static class TestUser {
        private Long id;
        private String name;
        private int age;

        public TestUser() {
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }
}