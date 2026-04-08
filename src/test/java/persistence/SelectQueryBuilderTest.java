package persistence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static persistence.SelectQueryBuilder.OrderDirection.ASC;

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

    @Test
    void ORDER_BY_절_포함된_쿼리_생성() {
        String sql = new SelectQueryBuilder()
                .select("id", "name")
                .from("users")
                .orderBy("name", ASC)
                .build();

        assertEquals("SELECT id, name FROM users ORDER BY name ASC", sql);
    }

    @Test
    void LIMIT_절_포함된_쿼리_생성() {
        String sql = new SelectQueryBuilder()
                .select("id", "name")
                .from("users")
                .limit(10)
                .build();

        assertEquals("SELECT id, name FROM users LIMIT 10", sql);
    }

    @Test
    void 음수_값으로_LIMIT_지정하면_예외_발생() {
        SelectQueryBuilder builder = new SelectQueryBuilder()
                .select("id", "name")
                .from("users");

        assertThrows(IllegalArgumentException.class, () -> builder.limit(-5));
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