package query;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import persistence.query.SelectQueryBuilder;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SelectQueryBuilderTest {
    @Test
    void 기본_SELECT_쿼리_생성() {
        String sql = new SelectQueryBuilder()
                .select("id", "name")
                .from("users")
                .build();

        assertEquals("SELECT id, name FROM users", sql);
    }

    @Test
    void SELECT_없이_호출하면_기본값_사용() {
        String sql = new SelectQueryBuilder()
                .from("users")
                .build();

        assertEquals("SELECT * FROM users", sql);
    }

    @Test
    void FROM_없이_빌드하면_예외_발생() {
        SelectQueryBuilder builder = new SelectQueryBuilder()
                .select("*");

        assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @ParameterizedTest
    @MethodSource("메서드_호출_순서_제공")
    void 메서드_호출_순서는_자유롭게_지정_가능(Function<SelectQueryBuilder, SelectQueryBuilder> builderFunction) {
        SelectQueryBuilder builder = new SelectQueryBuilder();
        String sql = builderFunction.apply(builder).build();

        assertEquals("SELECT id, name FROM users ORDER BY age DESC", sql);
    }

    static Stream<Function<SelectQueryBuilder, SelectQueryBuilder>> 메서드_호출_순서_제공() {
        return Stream.of(
                builder -> builder.select("id", "name").from("users").orderBy("age", "DESC"),
                builder -> builder.from("users").orderBy("age", "DESC").select("id", "name"),
                builder -> builder.orderBy("age", "DESC").from("users").select("id", "name")
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void limit은_양의_정수만_허용(int invalidLimit) {
        SelectQueryBuilder builder = new SelectQueryBuilder()
                .from("users");

        assertThrows(IllegalArgumentException.class, () -> builder.limit(invalidLimit));
    }

    @Test
    void 동일한_select_메서드_여러번_호출시_마지막_값으로_덮어쓰기() {
        String sql = new SelectQueryBuilder()
                .select("id")
                .select("name")
                .from("users")
                .build();

        assertEquals("SELECT name FROM users", sql);
    }

    @Test
    void SELECT에_WHERE_절_추가() {
        String sql = new SelectQueryBuilder()
                .select("id", "name")
                .from("users")
                .where("age >= ?")
                .build();

        assertEquals("SELECT id, name FROM users WHERE age >= ?", sql);
    }
}