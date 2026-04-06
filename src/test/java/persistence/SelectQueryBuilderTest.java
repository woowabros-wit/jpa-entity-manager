package persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SelectQueryBuilderTest {

    @Test
    void 기본_조회() {
        // 예시 1: 기본 조회
        String sql = new SelectQueryBuilder()
                .select("id", "name", "age")
                .from("users")
                .build();
        // 결과: "SELECT id, name, age FROM users"
        assertThat(sql).isEqualTo("SELECT id, name, age FROM users");
    }

    @Test
    void 정렬_추가() {

        // 예시 2: 정렬 추가
        String sql = new SelectQueryBuilder()
                .select("*")
                .from("users")
                .orderBy("age", "DESC")
                .build();
        // 결과: "SELECT * FROM users ORDER BY age DESC"

        assertThat(sql).isEqualTo("SELECT * FROM users ORDER BY age DESC");

    }

    @Test
    void LIMIT_추가() {
        // 예시 3: LIMIT 추가
        String sql = new SelectQueryBuilder()
                .select("id", "name")
                .from("users")
                .orderBy("id", "ASC")
                .limit(10)
                .build();

        assertThat(sql).isEqualTo("SELECT id, name FROM users ORDER BY id ASC LIMIT 10");
    }

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

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void LIMIT_음수_예외_발생() {
        SelectQueryBuilder builder = new SelectQueryBuilder()
                .select("*")
                .from("users")
                .limit(-1); // Look at this

        assertThrows(IllegalStateException.class, builder::build);
    }

}