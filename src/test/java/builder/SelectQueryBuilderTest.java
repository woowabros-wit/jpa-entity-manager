package builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
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
    void 컬럼_하나만_선택() {
        String sql = new SelectQueryBuilder()
            .select("email")
            .from("users")
            .build();

        assertEquals("SELECT email FROM users", sql);
    }

    @Test
    void 여러_컬럼_선택() {
        String sql = new SelectQueryBuilder()
            .select("id", "name", "email", "age")
            .from("users")
            .build();

        assertEquals("SELECT id, name, email, age FROM users", sql);
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

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void 빈_테이블명이면_예외_발생() {
        SelectQueryBuilder builder = new SelectQueryBuilder()
            .from("   ");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void ORDER_BY_ASC_쿼리_생성() {
        String sql = new SelectQueryBuilder()
            .select("id", "name")
            .from("users")
            .orderBy("name", Direction.ASC)
            .build();

        assertEquals("SELECT id, name FROM users ORDER BY name ASC", sql);
    }

    @Test
    void ORDER_BY_DESC_쿼리_생성() {
        String sql = new SelectQueryBuilder()
            .from("orders")
            .orderBy("created_at", Direction.DESC)
            .build();

        assertEquals("SELECT * FROM orders ORDER BY created_at DESC", sql);
    }

    @Test
    void ORDER_BY에서_Direction미지정_쿼리_생성() {
        String sql = new SelectQueryBuilder()
            .select("id", "name")
            .from("users")
            .orderBy("name", null)
            .limit(1)
            .build();

        assertEquals("SELECT id, name FROM users ORDER BY name LIMIT 1", sql);
    }

    @Test
    void LIMIT_쿼리_생성() {
        String sql = new SelectQueryBuilder()
            .select("id")
            .from("users")
            .limit(10)
            .build();

        assertEquals("SELECT id FROM users LIMIT 10", sql);
    }

    @Test
    void LIMIT_0이면_예외_발생() {
        SelectQueryBuilder builder = new SelectQueryBuilder();
        assertThrows(IllegalArgumentException.class, () -> builder.limit(0));
    }

    @Test
    void LIMIT_음수이면_예외_발생() {
        SelectQueryBuilder builder = new SelectQueryBuilder();
        assertThrows(IllegalArgumentException.class, () -> builder.limit(-1));
    }

    @Test
    void LIMIT만_있고_ORDER_BY_없는_쿼리() {
        String sql = new SelectQueryBuilder()
            .from("users")
            .limit(1)
            .build();

        assertEquals("SELECT * FROM users LIMIT 1", sql);
    }

    @Test
    void ORDER_BY만_있고_LIMIT_없는_쿼리() {
        String sql = new SelectQueryBuilder()
            .select("id", "name", "email")
            .from("users")
            .orderBy("id", Direction.ASC)
            .limit(5)
            .build();

        assertEquals("SELECT id, name, email FROM users ORDER BY id ASC LIMIT 5", sql);
    }

    @Test
    void 메서드_호출_순서가_달라도_동일한_결과() {
        String sql1 = new SelectQueryBuilder()
            .select("id")
            .from("users")
            .orderBy("id", Direction.DESC)
            .limit(3)
            .build();

        String sql2 = new SelectQueryBuilder()
            .from("users")
            .limit(3)
            .orderBy("id", Direction.DESC)
            .select("id")
            .build();

        assertEquals(sql1, sql2);
    }

    @Test
    void 아무것도_설정하지_않으면_예외_발생() {
        SelectQueryBuilder builder = new SelectQueryBuilder();
        assertThrows(IllegalStateException.class, builder::build);
    }
}
