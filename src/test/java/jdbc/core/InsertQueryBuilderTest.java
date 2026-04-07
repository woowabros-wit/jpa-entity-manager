package jdbc.core;

import jdbc.vo.NamedParameterBindCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InsertQueryBuilderTest {

    @Test
    @DisplayName("기본_INSERT_쿼리_생성_value로_컬럼값_하나씩_추가")
    void test01() {
        String sql = new InsertQueryBuilder()
                .into("users")
                .value("name", "?")
                .value("age", "?")
                .value("email", "?")
                .build();

        assertEquals("INSERT INTO users (name, age, email) VALUES (?, ?, ?)", sql);
    }

    @Test
    @DisplayName("Map을_사용한_INSERT_쿼리_생성_values로_한번에_추가")
    void test02() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("name", "?");
        values.put("age", "?");

        String sql = new InsertQueryBuilder()
                .into("users")
                .values(values)
                .build();

        assertEquals("INSERT INTO users (name, age) VALUES (?, ?)", sql);
    }

    @Test
    @DisplayName("컬럼값_쌍_없이_빌드하면_예외_발생")
    void test03() {
        InsertQueryBuilder builder = new InsertQueryBuilder()
                .into("users");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    @DisplayName("테이블명_없이_빌드하면_예외_발생")
    void test04() {
        InsertQueryBuilder builder = new InsertQueryBuilder()
                .value("name", "?");

        assertThrows(Exception.class, builder::build);
    }

    @Test
    @DisplayName("into에_null_전달_시_예외_발생")
    void test05() {
        assertThrows(IllegalArgumentException.class, () ->
                new InsertQueryBuilder().into(null));
    }

    @Test
    @DisplayName("value에_null_컬럼명_전달_시_예외_발생")
    void test06() {
        assertThrows(IllegalArgumentException.class, () ->
                new InsertQueryBuilder()
                        .into("users")
                        .value(null, "?"));
    }

    @Test
    @DisplayName("컬럼_하나만_있는_INSERT_쿼리_생성")
    void test07() {
        String sql = new InsertQueryBuilder()
                .into("users")
                .value("name", "?")
                .build();

        assertEquals("INSERT INTO users (name) VALUES (?)", sql);
    }

    @Test
    @DisplayName("LinkedHashMap_사용_시_컬럼_순서가_추가한_순서대로_유지된다")
    void test08() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("email", "?");
        values.put("name", "?");
        values.put("age", "?");

        String sql = new InsertQueryBuilder()
                .into("users")
                .values(values)
                .build();

        assertEquals("INSERT INTO users (email, name, age) VALUES (?, ?, ?)", sql);
    }

    @Test
    @DisplayName("value와_values를_혼합하여_사용할_수_있다")
    void test09() {
        Map<String, String> mapValues = new LinkedHashMap<>();
        mapValues.put("age", "?");
        mapValues.put("email", "?");

        String sql = new InsertQueryBuilder()
                .into("users")
                .value("name", "?")
                .values(mapValues)
                .build();

        assertEquals("INSERT INTO users (name, age, email) VALUES (?, ?, ?)", sql);
    }

    @Test
    @DisplayName("isSupported_INSERT_대문자로_시작하면_true")
    void test10() {
        InsertQueryBuilder builder = new InsertQueryBuilder();

        assertTrue(builder.isSupported("INSERT INTO users (name) VALUES (?)"));
    }

    @Test
    @DisplayName("isSupported_insert_소문자로_시작하면_true")
    void test11() {
        InsertQueryBuilder builder = new InsertQueryBuilder();

        assertTrue(builder.isSupported("insert into users (name) values (?)"));
    }

    @Test
    @DisplayName("isSupported_INSERT가_아닌_SQL이면_false")
    void test12() {
        InsertQueryBuilder builder = new InsertQueryBuilder();

        assertFalse(builder.isSupported("SELECT * FROM users"));
        assertFalse(builder.isSupported("UPDATE users SET name = ?"));
        assertFalse(builder.isSupported("DELETE FROM users"));
    }

    @Test
    @DisplayName("makeJdbcQuery_INSERT_SQL_문자열을_파싱하여_쿼리를_생성한다")
    void test13() {
        InsertQueryBuilder builder = new InsertQueryBuilder();

        String result = builder.makeJdbcQuery("INSERT INTO users (name, age) VALUES (?, ?)");

        assertEquals("INSERT INTO users (name, age) VALUES (?, ?)", result);
    }

    @Test
    @DisplayName("makeJdbcQuery_컬럼과_값_개수가_다르면_예외가_발생한다")
    void test14() {
        InsertQueryBuilder builder = new InsertQueryBuilder();

        assertThrows(IllegalArgumentException.class,
                () -> builder.makeJdbcQuery("INSERT INTO users (name, age) VALUES (?)"));
    }

    @Test
    @DisplayName("makeJdbcQuery_컬럼_하나짜리_INSERT를_파싱한다")
    void test15() {
        InsertQueryBuilder builder = new InsertQueryBuilder();

        String result = builder.makeJdbcQuery("INSERT INTO users (name) VALUES (?)");

        assertEquals("INSERT INTO users (name) VALUES (?)", result);
    }

    @Test
    @DisplayName("makeJdbcQuery_named_parameter가_포함된_INSERT를_파싱하면_물음표로_치환된다")
    void test16() {
        InsertQueryBuilder builder = new InsertQueryBuilder();

        String result = builder.makeJdbcQuery("INSERT INTO users (name, age) VALUES (:name, :age)");

        assertEquals("INSERT INTO users (name, age) VALUES (?, ?)", result);
    }

    @Test
    @DisplayName("processOrders_named_parameter에_순서를_부여한다")
    void test17() {
        InsertQueryBuilder builder = new InsertQueryBuilder();
        builder.into("users")
                .value("name", ":name")
                .value("age", ":age");

        List<NamedParameterBindCommand> commands = builder.getCommand();
        assertEquals(2, commands.size());
        assertEquals(1, commands.get(0).getOrder());
        assertEquals(2, commands.get(1).getOrder());
    }

    @Test
    @DisplayName("processOrders_named_parameter가_아닌_값은_순서를_부여하지_않는다")
    void test18() {
        InsertQueryBuilder builder = new InsertQueryBuilder();
        builder.into("users")
                .value("name", ":name")
                .value("age", "?")
                .value("email", ":email");

        List<NamedParameterBindCommand> commands = builder.getCommand();
        assertEquals(2, commands.size());
        assertEquals(1, commands.get(0).getOrder());
        assertEquals(2, commands.get(1).getOrder());
    }

    @Test
    @DisplayName("getCommand_named_parameter가_없으면_빈_리스트를_반환한다")
    void test19() {
        InsertQueryBuilder builder = new InsertQueryBuilder();
        builder.into("users")
                .value("name", "?")
                .value("age", "?");

        List<NamedParameterBindCommand> commands = builder.getCommand();

        assertTrue(commands.isEmpty());
    }

    @Test
    @DisplayName("processParameter_named_parameter에_값을_바인딩한다")
    void test20() {
        InsertQueryBuilder builder = new InsertQueryBuilder();
        builder.into("users")
                .value("name", ":name")
                .value("age", ":age");

        builder.processParameter("name", "kim");
        builder.processParameter("age", 25);

        List<NamedParameterBindCommand> commands = builder.getCommand();
        assertEquals("kim", commands.get(0).getValue());
        assertEquals(25, commands.get(1).getValue());
    }

    @Test
    @DisplayName("processParameter_존재하지_않는_이름이면_예외가_발생한다")
    void test21() {
        InsertQueryBuilder builder = new InsertQueryBuilder();
        builder.into("users")
                .value("name", ":name");

        assertThrows(IllegalArgumentException.class,
                () -> builder.processParameter("unknown", "value"));
    }

    @Test
    @DisplayName("processParameter_named_parameter가_없는_상태에서_호출하면_예외가_발생한다")
    void test22() {
        InsertQueryBuilder builder = new InsertQueryBuilder();
        builder.into("users")
                .value("name", "?");

        assertThrows(IllegalArgumentException.class,
                () -> builder.processParameter("name", "kim"));
    }
}
