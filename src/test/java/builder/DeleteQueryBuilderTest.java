package builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import builder.where.ComparisonCondition;
import builder.where.ComparisonOperator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
class DeleteQueryBuilderTest {

    @Test
    void 쿼리_생성() {
        ComparisonCondition comparisonCondition = new ComparisonCondition("age", ComparisonOperator.LT, "?");
        String sql = new DeleteQueryBuilder()
            .from("users")
            .where(comparisonCondition)
            .build();

        assertEquals("DELETE FROM users WHERE age < ?", sql);
    }

    @Test
    void WHERE_없으면_예외() {
        DeleteQueryBuilder builder = new DeleteQueryBuilder()
            .from("users");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void 테이블명_없으면_예외() {
        ComparisonCondition comparisonCondition = new ComparisonCondition("age", ComparisonOperator.LT, "?");
        DeleteQueryBuilder builder = new DeleteQueryBuilder()
            .where(comparisonCondition);

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void where_and_조건_추가() {
        String sql = new DeleteQueryBuilder()
            .from("users")
            .where(new ComparisonCondition("name", ComparisonOperator.EQ, "?"))
            .and(new ComparisonCondition("age", ComparisonOperator.GT, "?"))
            .build();

        assertEquals("DELETE FROM users WHERE (name = ?) AND (age > ?)", sql);
    }

    @Test
    void where_and_조건_2번_추가() {
        String sql = new DeleteQueryBuilder()
            .from("users")
            .where(new ComparisonCondition("name", ComparisonOperator.EQ, "?"))
            .and(new ComparisonCondition("age", ComparisonOperator.GT, "?"))
            .and(new ComparisonCondition("city", ComparisonOperator.EQ, "?"))
            .build();

        assertEquals("DELETE FROM users WHERE ((name = ?) AND (age > ?)) AND (city = ?)", sql);
    }

    @Test
    void where_or_조건_추가() {
        String sql = new DeleteQueryBuilder()
            .from("users")
            .where(new ComparisonCondition("name", ComparisonOperator.EQ, "?"))
            .or(new ComparisonCondition("age", ComparisonOperator.GT, "?"))
            .build();

        assertEquals("DELETE FROM users WHERE (name = ?) OR (age > ?)", sql);
    }

    @Test
    void where_and_or_조건_추가() {
        String sql = new DeleteQueryBuilder()
            .from("users")
            .where(new ComparisonCondition("name", ComparisonOperator.EQ, "?"))
            .and(new ComparisonCondition("age", ComparisonOperator.GT, "?"))
            .or(new ComparisonCondition("city", ComparisonOperator.EQ, "?"))
            .build();

        assertEquals("DELETE FROM users WHERE ((name = ?) AND (age > ?)) OR (city = ?)", sql);
    }
}
