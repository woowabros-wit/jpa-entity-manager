package persistence;

import persistence.annotation.Table;
import org.junit.jupiter.api.Test;
import persistence.fixture.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntityMetaQueryTest {

    @Test
    void findById_쿼리를_생성한다() {
        EntityMetaQuery query = new EntityMetaQuery(User.class);

        String sql = query.buildFindById();

        assertEquals("SELECT * FROM users WHERE id = ?", sql);
    }

    @Test
    void Table_어노테이션이_없으면_예외가_발생한다() {
        assertThrows(IllegalStateException.class,
                () -> new EntityMetaQuery(NoTableEntity.class));
    }

    @Test
    void Id_어노테이션이_없으면_예외가_발생한다() {
        assertThrows(IllegalStateException.class,
                () -> new EntityMetaQuery(NoIdEntity.class).buildFindById());
    }

    @Table(name = "no_id")
    static class NoIdEntity {
        private Long id;
    }

    static class NoTableEntity {
        private Long id;
    }
}
