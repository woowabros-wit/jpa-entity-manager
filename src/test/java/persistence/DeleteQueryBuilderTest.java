package persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeleteQueryBuilderTest {

    @Test
    void FROM과_WHERE를_지정하여_DELETE_쿼리를_생성한다() {
        String sql = new DeleteQueryBuilder()
                .from("users")
                .where("age < ?")
                .build();

        assertThat(sql).isEqualTo("DELETE FROM users WHERE age < ?");
    }

    @Test
    void 테이블을_지정하지_않으면_예외가_발생한다() {
        assertThatThrownBy(() -> new DeleteQueryBuilder()
                .where("age < ?")
                .build()
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void WHERE_조건이_없으면_예외가_발생한다() {
        assertThatThrownBy(() -> new DeleteQueryBuilder()
                .from("users")
                .build()
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void where를_여러번_호출하면_마지막_값으로_덮어쓴다() {
        String sql = new DeleteQueryBuilder()
                .from("users")
                .where("id = ?")
                .where("age < ?")
                .build();

        assertThat(sql).isEqualTo("DELETE FROM users WHERE age < ?");
    }
}
