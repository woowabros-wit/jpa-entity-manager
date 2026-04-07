package persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateQueryBuilderTest {
    /**
     * UPDATE 쿼리를 생성하여 기존 데이터를 수정할 수 있다.
     *      e.g. UPDATE users SET name = ?, age = ? WHERE id = ?
     *      SET 절에 여러 컬럼을 지정할 수 있다.
     *      WHERE 조건을 통해 수정 대상을 특정할 수 있다.
     *
     * UPDATE와 DELETE 쿼리는 WHERE 조건을 필수로 요구한다.
     *      WHERE 없는 UPDATE/DELETE는 위험하므로 빌드 시 예외를 발생시킨다.
     *      e.g. UPDATE users SET name = ? → 예외 발생 (WHERE 없음)
     *
     * 파라미터 값은 직접 쿼리 문자열에 포함하지 않고, 플레이스홀더(?)만 사용한다.
     *      e.g. "name = ?" (O), "name = 'John'" (X)
     *
     * WHERE 조건은 문자열 형태로 전달하며, AND/OR 조합은 이번 단계에서 제외한다.
     *      e.g. where("age >= ? AND status = ?") 형태로 한 번에 전달
     *
     */

    @Test
    void case1() {
        String sql = new UpdateQueryBuilder()
                .table("users")
                .set("name", "?")
                .set("age", "?")
                .where("id = ?")
                .build();

        assertThat(sql).isEqualTo("UPDATE users SET name = ?, age = ? WHERE id = ?");
    }

    @Test
    @DisplayName("UPDATE와 DELETE 쿼리는 WHERE 조건을 필수로 요구한다.")
    void case2() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder()
                .table("users")
                .set("name", "?");

        assertThrows(IllegalStateException.class, builder::build);
    }


    @Test
    @DisplayName("INSERT/UPDATE 시 컬럼 순서가 추가한 순서대로 유지된다.")
    void case3() {
        assertThat(new UpdateQueryBuilder()
                .table("users")
                .set("name", "?")
                .set("age", "?")
                .where("id = ?")
                .build()).isEqualTo("UPDATE users SET name = ?, age = ? WHERE id = ?");


        assertThat(new UpdateQueryBuilder()
                .table("users")
                .set("age", "?")
                .set("name", "?")
                .where("id = ?")
                .build()).isEqualTo("UPDATE users SET age = ?, name = ? WHERE id = ?");
    }
}