package builder;

import static builder.where.ComparisonOperator.EQ;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import builder.where.ComparisonCondition;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
class UpdateQueryBuilderTest {

    @Test
    void 쿼리_생성() {
        String sql = new UpdateQueryBuilder()
            .table("users")
            .set("name", "?")
            .set("age", "?")
            .where(new ComparisonCondition("id", EQ, "?"))
            .build();

        assertEquals("UPDATE users SET name = ?, age = ? WHERE id = ?", sql);
    }

    @Test
    void WHERE_없으면_예외() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder()
            .table("users")
            .set("name", "?");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void 테이블명_없으면_예외() {
        UpdateQueryBuilder builder = new UpdateQueryBuilder()
            .set("name", "?")
            .where(new ComparisonCondition("id", EQ, "?"));

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void where_and_조건_추가() {
        String sql = new UpdateQueryBuilder()
            .table("users")
            .set("name", "?")
            .where(new ComparisonCondition("name", EQ, "?"))
            .and(new ComparisonCondition("age", EQ, "?"))
            .build();

        assertEquals("UPDATE users SET name = ? WHERE (name = ?) AND (age = ?)", sql);
    }

    @Test
    void where_or_조건_추가() {
        String sql = new UpdateQueryBuilder()
            .table("users")
            .set("name", "?")
            .where(new ComparisonCondition("name", EQ, "?"))
            .or(new ComparisonCondition("age", EQ, "?"))
            .build();

        assertEquals("UPDATE users SET name = ? WHERE (name = ?) OR (age = ?)", sql);
    }

    @Test
    void where_조건_체이닝() {
        String sql = new UpdateQueryBuilder()
            .table("users")
            .set("name", "?")
            .where(new ComparisonCondition("name", EQ, "?"))
            .and(new ComparisonCondition("age", EQ, "?"))
            .or(new ComparisonCondition("id", EQ, "?"))
            .build();

        assertEquals("UPDATE users SET name = ? WHERE ((name = ?) AND (age = ?)) OR (id = ?)", sql);
    }
}
