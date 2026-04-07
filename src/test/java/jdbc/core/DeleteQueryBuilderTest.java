package jdbc.core;

import jdbc.vo.NamedParameterBindCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DeleteQueryBuilderTest {

    @Test
    @DisplayName("기본_DELETE_쿼리_생성_WHERE_조건_포함")
    void test01() {
        String sql = new DeleteQueryBuilder()
                .from("users")
                .where("age < ?")
                .build();

        assertEquals("DELETE FROM users WHERE age < ?", sql);
    }

    @Test
    @DisplayName("등호_조건을_사용한_DELETE_쿼리_생성")
    void test02() {
        String sql = new DeleteQueryBuilder()
                .from("users")
                .where("id = ?")
                .build();

        assertEquals("DELETE FROM users WHERE id = ?", sql);
    }

    @Test
    @DisplayName("WHERE_없이_빌드하면_예외_발생")
    void test03() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder()
                .from("users");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    @DisplayName("테이블명_없이_빌드하면_예외_발생")
    void test04() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder()
                .where("id = ?");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    @DisplayName("from에_null_전달_시_예외_발생")
    void test05() {
        assertThrows(IllegalArgumentException.class, () ->
                new DeleteQueryBuilder().from(null));
    }

    @Test
    @DisplayName("비교_연산자_>=를_사용한_WHERE_조건")
    void test06() {
        String sql = new DeleteQueryBuilder()
                .from("users")
                .where("age >= ?")
                .build();

        assertEquals("DELETE FROM users WHERE age >= ?", sql);
    }

    @Test
    @DisplayName("비교_연산자_<=를_사용한_WHERE_조건")
    void test07() {
        String sql = new DeleteQueryBuilder()
                .from("users")
                .where("age <= ?")
                .build();

        assertEquals("DELETE FROM users WHERE age <= ?", sql);
    }

    @Test
    @DisplayName("비교_연산자_!=를_사용한_WHERE_조건")
    void test08() {
        String sql = new DeleteQueryBuilder()
                .from("users")
                .where("status != ?")
                .build();

        assertEquals("DELETE FROM users WHERE status != ?", sql);
    }

    @Test
    @DisplayName("isSupported_DELETE_대문자로_시작하면_true")
    void test09() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder();

        assertTrue(builder.isSupported("DELETE FROM users WHERE id = 1"));
    }

    @Test
    @DisplayName("isSupported_delete_소문자로_시작하면_true")
    void test10() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder();

        assertTrue(builder.isSupported("delete from users where id = 1"));
    }

    @Test
    @DisplayName("isSupported_DELETE가_아닌_SQL이면_false")
    void test11() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder();

        assertFalse(builder.isSupported("SELECT * FROM users"));
        assertFalse(builder.isSupported("INSERT INTO users (name) VALUES (?)"));
        assertFalse(builder.isSupported("UPDATE users SET name = ?"));
    }

    @Test
    @DisplayName("makeJdbcQuery_DELETE_SQL_문자열을_파싱하여_쿼리를_생성한다")
    void test12() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder();

        String result = builder.makeJdbcQuery("DELETE FROM users WHERE id = 1");

        assertEquals("DELETE FROM users WHERE id = 1", result);
    }

    @Test
    @DisplayName("makeJdbcQuery_named_parameter가_포함된_DELETE를_파싱하면_물음표로_치환된다")
    void test13() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder();

        String result = builder.makeJdbcQuery("DELETE FROM users WHERE id = :id");

        assertEquals("DELETE FROM users WHERE id = ?", result);
    }

    @Test
    @DisplayName("makeJdbcQuery_AND_조건이_포함된_DELETE를_파싱한다")
    void test14() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder();

        String result = builder.makeJdbcQuery("DELETE FROM users WHERE age >= 20 AND status = active");

        assertEquals("DELETE FROM users WHERE age >= 20 AND status = active", result);
    }

    @Test
    @DisplayName("processOrders_named_parameter에_순서를_부여한다")
    void test15() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder();
        builder.from("users")
                .where("age >= :minAge AND name = :name");

        List<NamedParameterBindCommand> commands = builder.getCommand();
        assertEquals(2, commands.size());
        assertEquals(1, commands.get(0).getOrder());
        assertEquals(2, commands.get(1).getOrder());
    }

    @Test
    @DisplayName("processOrders_named_parameter가_아닌_값은_순서를_부여하지_않는다")
    void test16() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder();
        builder.from("users")
                .where("age >= ? AND name = :name");

        List<NamedParameterBindCommand> commands = builder.getCommand();
        assertEquals(1, commands.size());
        assertEquals(1, commands.get(0).getOrder());
    }

    @Test
    @DisplayName("getCommand_named_parameter가_없으면_빈_리스트를_반환한다")
    void test17() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder();
        builder.from("users")
                .where("id = ?");

        List<NamedParameterBindCommand> commands = builder.getCommand();

        assertTrue(commands.isEmpty());
    }

    @Test
    @DisplayName("processParameter_named_parameter에_값을_바인딩한다")
    void test18() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder();
        builder.from("users")
                .where("id = :id");

        builder.processParameter("id", 42);

        List<NamedParameterBindCommand> commands = builder.getCommand();
        assertEquals(1, commands.size());
        assertEquals(42, commands.get(0).getValue());
    }

    @Test
    @DisplayName("processParameter_존재하지_않는_이름이면_예외가_발생한다")
    void test19() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder();
        builder.from("users")
                .where("id = :id");

        assertThrows(IllegalArgumentException.class,
                () -> builder.processParameter("unknown", "value"));
    }

    @Test
    @DisplayName("processParameter_named_parameter가_없는_상태에서_호출하면_예외가_발생한다")
    void test20() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder();
        builder.from("users")
                .where("id = ?");

        assertThrows(IllegalArgumentException.class,
                () -> builder.processParameter("id", 1));
    }
}
