package jdbc.core;

import jdbc.vo.NamedParameterBindCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SelectQueryBuilderTest {

    @Test
    @DisplayName("기본_SELECT_쿼리_생성")
    void test01() {
        String sql = new SelectQueryBuilder()
                .select("id", "name")
                .from("users")
                .build();

        assertEquals("SELECT id, name FROM users", sql);
    }

    @Test
    @DisplayName("SELECT_없이_호출하면_기본값_사용")
    void test02() {
        String sql = new SelectQueryBuilder()
                .from("users")
                .build();

        assertEquals("SELECT * FROM users", sql);
    }

    @Test
    @DisplayName("FROM_없이_빌드하면_예외_발생")
    void test03() {
        SelectQueryBuilder builder = new SelectQueryBuilder()
                .select("*");

        assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    @DisplayName("WHERE_조건을_포함한_SELECT_쿼리_생성")
    void test04() {
        String sql = new SelectQueryBuilder()
                .select("*")
                .from("users")
                .where("age >= ?")
                .build();

        assertEquals("SELECT * FROM users WHERE age >= ?", sql);
    }

    @Test
    @DisplayName("WHERE_조건과_ORDER_BY를_함께_사용한_SELECT_쿼리_생성")
    void test05() {
        String sql = new SelectQueryBuilder()
                .select("*")
                .from("users")
                .where("age >= ?")
                .orderBy("name", "ASC")
                .build();

        assertEquals("SELECT * FROM users WHERE age >= ? ORDER BY name ASC", sql);
    }

    @Test
    @DisplayName("등호_조건을_사용한_WHERE_절")
    void test06() {
        String sql = new SelectQueryBuilder()
                .select("name", "email")
                .from("users")
                .where("id = ?")
                .build();

        assertEquals("SELECT name, email FROM users WHERE id = ?", sql);
    }

    @Test
    @DisplayName("비교_연산자_<를_사용한_WHERE_절")
    void test07() {
        String sql = new SelectQueryBuilder()
                .select("*")
                .from("users")
                .where("age < ?")
                .build();

        assertEquals("SELECT * FROM users WHERE age < ?", sql);
    }

    @Test
    @DisplayName("WHERE_없이_ORDER_BY만_사용한_SELECT_쿼리_생성")
    void test08() {
        String sql = new SelectQueryBuilder()
                .select("*")
                .from("users")
                .orderBy("name", "DESC")
                .build();

        assertEquals("SELECT * FROM users ORDER BY name DESC", sql);
    }

    @Test
    @DisplayName("WHERE_ORDER_BY_LIMIT을_모두_사용한_SELECT_쿼리_생성")
    void test09() {
        String sql = new SelectQueryBuilder()
                .select("*")
                .from("users")
                .where("age >= ?")
                .orderBy("name", "ASC")
                .limit(10)
                .build();

        assertEquals("SELECT * FROM users WHERE age >= ? ORDER BY name ASC LIMIT 10", sql);
    }

    @Test
    @DisplayName("from에_null_전달_시_예외_발생")
    void test10() {
        assertThrows(IllegalStateException.class, () ->
                new SelectQueryBuilder().from(null));
    }

    @Test
    @DisplayName("limit에_0_이하_값_전달_시_예외_발생")
    void test11() {
        assertThrows(IllegalArgumentException.class, () ->
                new SelectQueryBuilder()
                        .select("*")
                        .from("users")
                        .limit(0));
    }

    @Test
    @DisplayName("비교_연산자_!=를_사용한_WHERE_절")
    void test12() {
        String sql = new SelectQueryBuilder()
                .select("*")
                .from("users")
                .where("status != ?")
                .build();

        assertEquals("SELECT * FROM users WHERE status != ?", sql);
    }

    @Test
    @DisplayName("isSupported_SELECT_대문자로_시작하면_true")
    void test13() {
        SelectQueryBuilder builder = new SelectQueryBuilder();

        assertTrue(builder.isSupported("SELECT * FROM users"));
    }

    @Test
    @DisplayName("isSupported_select_소문자로_시작하면_true")
    void test14() {
        SelectQueryBuilder builder = new SelectQueryBuilder();

        assertTrue(builder.isSupported("select * from users"));
    }

    @Test
    @DisplayName("isSupported_SELECT가_아닌_SQL이면_false")
    void test15() {
        SelectQueryBuilder builder = new SelectQueryBuilder();

        assertFalse(builder.isSupported("INSERT INTO users (name) VALUES (?)"));
        assertFalse(builder.isSupported("UPDATE users SET name = ?"));
        assertFalse(builder.isSupported("DELETE FROM users WHERE id = 1"));
    }

    @Test
    @DisplayName("makeJdbcQuery_SELECT_FROM만_있는_SQL을_파싱한다")
    void test16() {
        SelectQueryBuilder builder = new SelectQueryBuilder();

        String result = builder.makeJdbcQuery("SELECT id, name FROM users");

        assertEquals("SELECT id, name FROM users", result);
    }

    @Test
    @DisplayName("makeJdbcQuery_WHERE_조건이_포함된_SQL을_파싱한다")
    void test17() {
        SelectQueryBuilder builder = new SelectQueryBuilder();

        String result = builder.makeJdbcQuery("SELECT * FROM users WHERE age >= 20");

        assertEquals("SELECT * FROM users WHERE age >= 20", result);
    }

    @Test
    @DisplayName("makeJdbcQuery_WHERE_ORDER_BY_LIMIT이_모두_포함된_SQL을_파싱한다")
    void test18() {
        SelectQueryBuilder builder = new SelectQueryBuilder();

        String result = builder.makeJdbcQuery("SELECT * FROM users WHERE age >= 20 ORDER BY name ASC LIMIT 10");

        assertEquals("SELECT * FROM users WHERE age >= 20 ORDER BY name ASC LIMIT 10", result);
    }

    @Test
    @DisplayName("makeJdbcQuery_named_parameter가_포함된_SQL을_파싱하면_물음표로_치환된다")
    void test19() {
        SelectQueryBuilder builder = new SelectQueryBuilder();

        String result = builder.makeJdbcQuery("SELECT * FROM users WHERE id = :id");

        assertEquals("SELECT * FROM users WHERE id = ?", result);
    }

    @Test
    @DisplayName("makeJdbcQuery_ORDER_BY만_있고_WHERE가_없는_SQL을_파싱한다")
    void test20() {
        SelectQueryBuilder builder = new SelectQueryBuilder();

        String result = builder.makeJdbcQuery("SELECT * FROM users ORDER BY name DESC");

        assertEquals("SELECT * FROM users ORDER BY name DESC", result);
    }

    @Test
    @DisplayName("processOrders_named_parameter에_순서를_부여한다")
    void test21() {
        SelectQueryBuilder builder = new SelectQueryBuilder();
        builder.select("*")
                .from("users")
                .where("age >= :minAge AND name = :name");

        List<NamedParameterBindCommand> commands = builder.getCommand();
        assertEquals(2, commands.size());
        assertEquals(1, commands.get(0).getOrder());
        assertEquals(2, commands.get(1).getOrder());
    }

    @Test
    @DisplayName("processOrders_named_parameter가_아닌_값은_순서를_부여하지_않는다")
    void test22() {
        SelectQueryBuilder builder = new SelectQueryBuilder();
        builder.select("*")
                .from("users")
                .where("age >= ? AND name = :name");

        List<NamedParameterBindCommand> commands = builder.getCommand();
        assertEquals(1, commands.size());
        assertEquals(1, commands.get(0).getOrder());
    }

    @Test
    @DisplayName("getCommand_named_parameter가_없으면_빈_리스트를_반환한다")
    void test23() {
        SelectQueryBuilder builder = new SelectQueryBuilder();
        builder.select("*")
                .from("users")
                .where("id = ?");

        List<NamedParameterBindCommand> commands = builder.getCommand();

        assertTrue(commands.isEmpty());
    }

    @Test
    @DisplayName("processParameter_named_parameter에_값을_바인딩한다")
    void test24() {
        SelectQueryBuilder builder = new SelectQueryBuilder();
        builder.select("*")
                .from("users")
                .where("age >= :minAge AND name = :name");

        builder.processParameter("minAge", 20);
        builder.processParameter("name", "kim");

        List<NamedParameterBindCommand> commands = builder.getCommand();
        assertEquals(2, commands.size());
        assertEquals(20, commands.get(0).getValue());
        assertEquals("kim", commands.get(1).getValue());
    }

    @Test
    @DisplayName("processParameter_존재하지_않는_이름이면_예외가_발생한다")
    void test25() {
        SelectQueryBuilder builder = new SelectQueryBuilder();
        builder.select("*")
                .from("users")
                .where("id = :id");

        assertThrows(IllegalArgumentException.class,
                () -> builder.processParameter("unknown", "value"));
    }

    @Test
    @DisplayName("processParameter_named_parameter가_없는_상태에서_호출하면_예외가_발생한다")
    void test26() {
        SelectQueryBuilder builder = new SelectQueryBuilder();
        builder.select("*")
                .from("users")
                .where("id = ?");

        assertThrows(IllegalArgumentException.class,
                () -> builder.processParameter("id", 1));
    }
}