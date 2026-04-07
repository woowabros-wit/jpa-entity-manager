package persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeleteQueryBuilderTest {

    @Test
    void case1() {
        String sql = new DeleteQueryBuilder()
                .from("users")
                .where("age < ?")
                .build();

        assertThat(sql).isEqualTo("DELETE FROM users WHERE age < ?");
    }

    @Test
    @DisplayName("UPDATE와 DELETE 쿼리는 WHERE 조건을 필수로 요구한다.")
    void case2() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder()
                .from("users");

        assertThrows(IllegalStateException.class, builder::build);
    }

}