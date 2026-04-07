package jdbc.core;

import jdbc.vo.NamedParameterBindCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UpdateQueryBuilderTest {

    @Test
    @DisplayName("기본_UPDATE_쿼리_생성_SET과_WHERE_포함")
    void test01() {
        String sql = new UpdateQueryBuilder()
                .table("users")
                .set("name", "?")
                .set("age", "?")
                .where("id = ?")
                .build();

        assertEquals("UPDATE users SET name = ?, age = ? WHERE id = ?", sql);
    }

    @Test
    @DisplayName("SET_절에_컬럼_하나만_지정한_UPDATE_쿼리_생성")
    void test02() {
        String sql = new UpdateQueryBuilder()
                .table("users")
                .set("name", "?")
                .where("id = ?")
                .build();

        assertEquals("UPDATE users SET name = ? WHERE id = ?", sql);
    }

    @Test
    @DisplayName("WHERE_없이_빌드하면_예외_발생")
    void test03() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder()
                .table("users")
                .set("name", "?");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    @DisplayName("테이블명_없이_빌드하면_예외_발생")
    void test04() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder()
                .set("name", "?")
                .where("id = ?");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    @DisplayName("table에_null_전달_시_예외_발생")
    void test05() {
        assertThrows(IllegalArgumentException.class, () ->
                new UpdateQueryBuilder().table(null));
    }

    @Test
    @DisplayName("set에_null_컬럼명_전달_시_예외_발생")
    void test06() {
        assertThrows(IllegalArgumentException.class, () ->
                new UpdateQueryBuilder()
                        .table("users")
                        .set(null, "?"));
    }

    @Test
    @DisplayName("비교_연산자_<를_사용한_WHERE_조건")
    void test08() {
        String sql = new UpdateQueryBuilder()
                .table("users")
                .set("status", "?")
                .where("age < ?")
                .build();

        assertEquals("UPDATE users SET status = ? WHERE age < ?", sql);
    }

    @Test
    @DisplayName("비교_연산자_>=를_사용한_WHERE_조건")
    void test09() {
        String sql = new UpdateQueryBuilder()
                .table("users")
                .set("status", "?")
                .where("age >= ?")
                .build();

        assertEquals("UPDATE users SET status = ? WHERE age >= ?", sql);
    }

    @Test
    @DisplayName("SET_절의_컬럼_순서가_추가한_순서대로_유지된다")
    void test10() {
        String sql = new UpdateQueryBuilder()
                .table("users")
                .set("email", "?")
                .set("name", "?")
                .set("age", "?")
                .where("id = ?")
                .build();

        assertEquals("UPDATE users SET email = ?, name = ?, age = ? WHERE id = ?", sql);
    }

    @Test
    @DisplayName("isSupported_UPDATE_대문자로_시작하면_true")
    void test11() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder();

        assertTrue(builder.isSupported("UPDATE users SET name = ? WHERE id = 1"));
    }

    @Test
    @DisplayName("isSupported_update_소문자로_시작하면_true")
    void test12() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder();

        assertTrue(builder.isSupported("update users set name = ? where id = 1"));
    }

    @Test
    @DisplayName("isSupported_UPDATE가_아닌_SQL이면_false")
    void test13() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder();

        assertFalse(builder.isSupported("SELECT * FROM users"));
        assertFalse(builder.isSupported("INSERT INTO users (name) VALUES (?)"));
        assertFalse(builder.isSupported("DELETE FROM users WHERE id = 1"));
    }

    @Test
    @DisplayName("makeJdbcQuery_UPDATE_SQL_문자열을_파싱하여_쿼리를_생성한다")
    void test14() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder();

        String result = builder.makeJdbcQuery("UPDATE users SET name = ? WHERE id = ?");

        assertEquals("UPDATE users SET name = ? WHERE id = ?", result);
    }

    @Test
    @DisplayName("makeJdbcQuery_여러_SET_컬럼이_포함된_UPDATE를_파싱한다")
    void test15() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder();

        String result = builder.makeJdbcQuery("UPDATE users SET name = ?, age = ? WHERE id = ?");

        assertEquals("UPDATE users SET name = ?, age = ? WHERE id = ?", result);
    }

    @Test
    @DisplayName("makeJdbcQuery_named_parameter가_포함된_UPDATE를_파싱하면_물음표로_치환된다")
    void test16() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder();

        String result = builder.makeJdbcQuery("UPDATE users SET name = :name WHERE id = :id");

        assertEquals("UPDATE users SET name = ? WHERE id = ?", result);
    }

    @Test
    @DisplayName("processOrders_SET과_WHERE의_named_parameter에_순서를_부여한다")
    void test17() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder();
        builder.table("users")
                .set("name", ":name")
                .set("age", ":age")
                .where("id = :id");

        List<NamedParameterBindCommand> commands = builder.getCommand();
        assertEquals(3, commands.size());
        assertEquals(1, commands.get(0).getOrder());
        assertEquals(2, commands.get(1).getOrder());
        assertEquals(3, commands.get(2).getOrder());
    }

    @Test
    @DisplayName("processOrders_named_parameter가_아닌_값은_순서를_부여하지_않는다")
    void test18() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder();
        builder.table("users")
                .set("name", ":name")
                .set("age", "?")
                .where("id = :id");

        List<NamedParameterBindCommand> commands = builder.getCommand();
        assertEquals(2, commands.size());
        assertEquals(1, commands.get(0).getOrder());
        assertEquals(2, commands.get(1).getOrder());
    }

    @Test
    @DisplayName("getCommand_named_parameter가_없으면_빈_리스트를_반환한다")
    void test19() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder();
        builder.table("users")
                .set("name", "?")
                .where("id = ?");

        List<NamedParameterBindCommand> commands = builder.getCommand();

        assertTrue(commands.isEmpty());
    }

    @Test
    @DisplayName("processParameter_SET과_WHERE_모두에_같은_named_parameter가_있으면_같은_값이_바인딩된다")
    void test20() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder();
        builder.table("users")
                .set("name", ":name")
                .where("name = :name");

        builder.processParameter("name", "kim");

        List<NamedParameterBindCommand> commands = builder.getCommand();
        assertEquals(2, commands.size());
        assertEquals("kim", commands.get(0).getValue());
        assertEquals("kim", commands.get(1).getValue());
    }

    @Test
    @DisplayName("processParameter_SET과_WHERE에_각각_다른_named_parameter가_있을_때_값을_바인딩한다")
    void test21() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder();
        builder.table("users")
                .set("name", ":name")
                .where("id = :id");

        builder.processParameter("name", "kim");
        builder.processParameter("id", 42);

        List<NamedParameterBindCommand> commands = builder.getCommand();
        assertEquals(2, commands.size());
        assertEquals("kim", commands.get(0).getValue());
        assertEquals(42, commands.get(1).getValue());
    }

    @Test
    @DisplayName("processParameter_존재하지_않는_이름이면_예외가_발생한다")
    void test22() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder();
        builder.table("users")
                .set("name", ":name")
                .where("id = :id");

        assertThrows(IllegalArgumentException.class,
                () -> builder.processParameter("unknown", "value"));
    }

    @Test
    @DisplayName("processParameter_named_parameter가_없는_상태에서_호출하면_예외가_발생한다")
    void test23() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder();
        builder.table("users")
                .set("name", "?")
                .where("id = ?");

        assertThrows(IllegalArgumentException.class,
                () -> builder.processParameter("name", "kim"));
    }
}
