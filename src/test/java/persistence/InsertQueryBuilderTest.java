package persistence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InsertQueryBuilderTest {
    @Test
    void INSERT_쿼리_생성() {
        String sql = new InsertQueryBuilder()
                .into("users")
                .value("name", "?")
                .value("age", "?")
                .build();

        assertEquals("INSERT INTO users (name, age) VALUES (?, ?)", sql);
    }

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
    void UPDATE에서_WHERE_없으면_예외() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder()
                .table("users")
                .set("name", "?");

        assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    void DELETE_쿼리_생성() {
        String sql = new DeleteQueryBuilder()
                .from("users")
                .where("age < ?")
                .build();

        assertEquals("DELETE FROM users WHERE age < ?", sql);
    }

    @Test
    void DELETE에서_WHERE_없으면_예외() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder()
                .from("users");

        assertThrows(IllegalStateException.class, () -> builder.build());
    }
//
//    @Test
//    void SELECT에_WHERE_절_추가() {
//        String sql = new SelectQueryBuilder()
//                .select("id", "name")
//                .from("users")
//                .where("age >= ?")
//                .build();
//
//        assertEquals("SELECT id, name FROM users WHERE age >= ?", sql);
//    }

}