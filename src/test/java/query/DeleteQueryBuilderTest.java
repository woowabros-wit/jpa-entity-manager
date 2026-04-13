package query;

import org.junit.jupiter.api.Test;
import persistence.query.DeleteQueryBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeleteQueryBuilderTest {

    @Test
    void DELETE_쿼리_생성() {
        String sql = new DeleteQueryBuilder()
                .from("users")
                .where("age < ?")
                .build();

        assertEquals("DELETE FROM users WHERE age < ?", sql);
    }

    @Test
    void DELETE_쿼리_생성_복잡한_WHERE() {
        String sql = new DeleteQueryBuilder()
                .from("users")
                .where("age >= ? AND status = ?")
                .build();

        assertEquals("DELETE FROM users WHERE age >= ? AND status = ?", sql);
    }

    @Test
    void DELETE_쿼리_테이블명_없으면_예외() {
        assertThrows(IllegalStateException.class, () -> {
            new DeleteQueryBuilder()
                    .where("age < ?")
                    .build();
        });
    }

    @Test
    void DELETE_쿼리_WHERE_없으면_예외() {
        assertThrows(IllegalStateException.class, () -> {
            new DeleteQueryBuilder()
                    .from("users")
                    .build();
        });
    }

    @Test
    void DELETE_쿼리_WHERE_빈_문자열이면_예외() {
        assertThrows(IllegalStateException.class, () -> {
            new DeleteQueryBuilder()
                    .from("users")
                    .where("")
                    .build();
        });
    }
}