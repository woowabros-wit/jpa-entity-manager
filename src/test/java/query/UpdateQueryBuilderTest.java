package query;

import org.junit.jupiter.api.Test;
import persistence.query.UpdateQueryBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateQueryBuilderTest {

    @Test
    void UPDATE_쿼리_생성() {
        String sql = new UpdateQueryBuilder()
                .table("users")
                .set("name", "?")
                .set("age", "?")
                .where("id = ?")
                .build();

        assertEquals("UPDATE users SET name = ?, age = ? WHERE id = ?", sql);
    }

    @Test
    void UPDATE_쿼리_생성_Map_사용() {
        Map<String, String> updates = new LinkedHashMap<>();
        updates.put("name", "?");
        updates.put("age", "?");

        String sql = new UpdateQueryBuilder()
                .table("users")
                .set(updates)
                .where("id = ?")
                .build();

        assertEquals("UPDATE users SET name = ?, age = ? WHERE id = ?", sql);
    }

    @Test
    void UPDATE_쿼리_생성_복잡한_WHERE() {
        String sql = new UpdateQueryBuilder()
                .table("users")
                .set("status", "?")
                .where("age >= ? AND status = ?")
                .build();

        assertEquals("UPDATE users SET status = ? WHERE age >= ? AND status = ?", sql);
    }

    @Test
    void UPDATE_쿼리_컬럼_순서_보장() {
        String sql = new UpdateQueryBuilder()
                .table("users")
                .set("name", "?")
                .set("age", "?")
                .set("email", "?")
                .set("status", "?")
                .where("id = ?")
                .build();

        assertEquals("UPDATE users SET name = ?, age = ?, email = ?, status = ? WHERE id = ?", sql);
    }

    @Test
    void UPDATE_쿼리_테이블명_없으면_예외() {
        assertThrows(IllegalStateException.class, () -> {
            new UpdateQueryBuilder()
                    .set("name", "?")
                    .where("id = ?")
                    .build();
        });
    }

    @Test
    void UPDATE_쿼리_SET_없으면_예외() {
        assertThrows(IllegalStateException.class, () -> {
            new UpdateQueryBuilder()
                    .table("users")
                    .where("id = ?")
                    .build();
        });
    }

    @Test
    void UPDATE_쿼리_WHERE_없으면_예외() {
        assertThrows(IllegalStateException.class, () -> {
            new UpdateQueryBuilder()
                    .table("users")
                    .set("name", "?")
                    .build();
        });
    }
}