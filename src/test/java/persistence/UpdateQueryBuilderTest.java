package persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UpdateQueryBuilderTest {

    @Test
    void SET과_WHERE를_지정하여_UPDATE_쿼리를_생성한다() {
        String sql = new UpdateQueryBuilder()
                .table("users")
                .set("name", "?")
                .set("age", "?")
                .where("id = ?")
                .build();

        assertThat(sql).isEqualTo("UPDATE users SET name = ?, age = ? WHERE id = ?");
    }

    @Test
    void 테이블을_지정하지_않으면_예외가_발생한다() {
        assertThatThrownBy(() -> new UpdateQueryBuilder()
                .set("name", "?")
                .where("id = ?")
                .build()
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void SET_절이_없으면_예외가_발생한다() {
        assertThatThrownBy(() -> new UpdateQueryBuilder()
                .table("users")
                .where("id = ?")
                .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void WHERE_조건이_없으면_예외가_발생한다() {
        assertThatThrownBy(() -> new UpdateQueryBuilder()
                .table("users")
                .set("name", "?")
                .build()
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 동일_컬럼을_여러번_set하면_마지막_값으로_덮어쓴다() {
        String sql = new UpdateQueryBuilder()
                .table("users")
                .set("name", "?")
                .set("name", "?")
                .set("age", "?")
                .where("id = ?")
                .build();

        assertThat(sql).isEqualTo("UPDATE users SET name = ?, age = ? WHERE id = ?");
    }
}
