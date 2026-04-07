package jdbc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NamedParameterQueryTest {

    @Test
    @DisplayName("SELECT_SQL로_생성하면_JDBC_쿼리를_반환한다")
    void test01() {
        NamedParameterQuery query = new NamedParameterQuery("SELECT * FROM users WHERE id = :id");

        assertEquals("SELECT * FROM users WHERE id = ?", query.toJdbcSql());
    }

    @Test
    @DisplayName("INSERT_SQL로_생성하면_JDBC_쿼리를_반환한다")
    void test02() {
        NamedParameterQuery query = new NamedParameterQuery("INSERT INTO users (name, age) VALUES (:name, :age)");

        assertEquals("INSERT INTO users (name, age) VALUES (?, ?)", query.toJdbcSql());
    }

    @Test
    @DisplayName("DELETE_SQL로_생성하면_JDBC_쿼리를_반환한다")
    void test03() {
        NamedParameterQuery query = new NamedParameterQuery("DELETE FROM users WHERE id = :id");

        assertEquals("DELETE FROM users WHERE id = ?", query.toJdbcSql());
    }

    @Test
    @DisplayName("UPDATE_SQL로_생성하면_JDBC_쿼리를_반환한다")
    void test04() {
        NamedParameterQuery query = new NamedParameterQuery("UPDATE users SET name = :name WHERE id = :id");

        assertEquals("UPDATE users SET name = ? WHERE id = ?", query.toJdbcSql());
    }

    @Test
    @DisplayName("지원하지_않는_SQL이면_예외가_발생한다")
    void test05() {
        assertThrows(IllegalArgumentException.class,
                () -> new NamedParameterQuery("CREATE TABLE users (id INT)"));
    }

    @Test
    @DisplayName("물음표_플레이스홀더_SQL도_정상_변환된다")
    void test06() {
        NamedParameterQuery query = new NamedParameterQuery("SELECT * FROM users WHERE id = ?");

        assertEquals("SELECT * FROM users WHERE id = ?", query.toJdbcSql());
    }

    @Test
    @DisplayName("setParameter_호출_시_예외가_발생하지_않는다")
    void test07() {
        NamedParameterQuery query = new NamedParameterQuery("SELECT * FROM users WHERE id = :id");

        assertDoesNotThrow(() -> query.setParameter("id", 42));
    }

    @Test
    @DisplayName("setParameter_여러번_체이닝_호출_시_예외가_발생하지_않는다")
    void test08() {
        NamedParameterQuery query = new NamedParameterQuery("SELECT * FROM users WHERE age >= :minAge AND name = :name");

        assertDoesNotThrow(() ->
                query.setParameter("minAge", 20)
                        .setParameter("name", "kim"));
    }

    @Test
    @DisplayName("setParameter에_존재하지_않는_이름을_전달하면_예외가_발생한다")
    void test09() {
        NamedParameterQuery query = new NamedParameterQuery("SELECT * FROM users WHERE id = :id");

        assertThrows(IllegalArgumentException.class,
                () -> query.setParameter("unknown", "value"));
    }

    @Test
    @DisplayName("named_parameter가_없는_SQL에서_setParameter를_호출하면_예외가_발생한다")
    void test10() {
        NamedParameterQuery query = new NamedParameterQuery("SELECT * FROM users WHERE id = ?");

        assertThrows(IllegalArgumentException.class,
                () -> query.setParameter("id", 1));
    }

    @Test
    @DisplayName("SELECT_여러_컬럼과_복합_조건을_가진_SQL을_변환한다")
    void test11() {
        NamedParameterQuery query = new NamedParameterQuery(
                "SELECT id, name, email FROM users WHERE age >= :minAge ORDER BY name ASC LIMIT 10");

        assertEquals("SELECT id, name, email FROM users WHERE age >= ? ORDER BY name ASC LIMIT 10",
                query.toJdbcSql());
    }

    @Test
    @DisplayName("INSERT_여러_named_parameter를_물음표로_치환한다")
    void test12() {
        NamedParameterQuery query = new NamedParameterQuery(
                "INSERT INTO users (name, age, email) VALUES (:name, :age, :email)");

        assertEquals("INSERT INTO users (name, age, email) VALUES (?, ?, ?)",
                query.toJdbcSql());
    }

    @Test
    @DisplayName("소문자_SQL도_정상적으로_JDBC_쿼리로_변환된다")
    void test13() {
        NamedParameterQuery query = new NamedParameterQuery("select * from users where id = :id");

        assertEquals("SELECT * FROM users WHERE id = ?", query.toJdbcSql());
    }
}
