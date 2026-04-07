package builder.where;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
class WhereClauseTest {

    @Test
    void where조건_추가() {
        WhereClause whereClause = WhereClause.empty();

        whereClause.where(new ComparisonCondition("age", ComparisonOperator.GT, "20"));

        assertEquals("WHERE age > 20", whereClause.toSql());
    }

    @Test
    void 단일where조건() {
        WhereClause whereClause = WhereClause.empty()
            .where(new ComparisonCondition("name", ComparisonOperator.EQ, "'kim'"));

        assertEquals("WHERE name = 'kim'", whereClause.toSql());
    }

    @Test
    void and_추가() {
        WhereClause whereClause = WhereClause.empty()
            .where(new ComparisonCondition("age", ComparisonOperator.GTE, "18"))
            .and(new ComparisonCondition("age", ComparisonOperator.LTE, "30"));

        assertEquals("WHERE (age >= 18) AND (age <= 30)", whereClause.toSql());
    }

    @Test
    void or_추가() {
        WhereClause whereClause = WhereClause.empty()
            .where(new ComparisonCondition("name", ComparisonOperator.EQ, "'kim'"))
            .or(new ComparisonCondition("name", ComparisonOperator.EQ, "'lee'"));

        assertEquals("WHERE (name = 'kim') OR (name = 'lee')", whereClause.toSql());
    }

    @Test
    void and_여러번_추가() {
        WhereClause whereClause = WhereClause.empty()
            .where(new ComparisonCondition("age", ComparisonOperator.GTE, "20"))
            .and(new ComparisonCondition("age", ComparisonOperator.LTE, "30"))
            .and(new ComparisonCondition("status", ComparisonOperator.EQ, "'ACTIVE'"));

        assertEquals("WHERE ((age >= 20) AND (age <= 30)) AND (status = 'ACTIVE')", whereClause.toSql());
    }

    @Test
    void and_or_혼합_추가() {
        WhereClause whereClause = WhereClause.empty()
            .where(new ComparisonCondition("age", ComparisonOperator.GTE, "20"))
            .and(new ComparisonCondition("age", ComparisonOperator.LTE, "30"))
            .or(new ComparisonCondition("role", ComparisonOperator.EQ, "'ADMIN'"));

        assertEquals("WHERE ((age >= 20) AND (age <= 30)) OR (role = 'ADMIN')", whereClause.toSql());
    }

    @Test
    void where없이_and_호출하면_예외() {
        WhereClause whereClause = WhereClause.empty();
        ComparisonCondition comparisonCondition = new ComparisonCondition("age", ComparisonOperator.GT, "20");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> whereClause.and(comparisonCondition));

        assertEquals("WHERE 를 우선 지정해주세요.", exception.getMessage());
    }

    @Test
    void where없이_or_호출하면_예외() {
        WhereClause whereClause = WhereClause.empty();
        ComparisonCondition comparisonCondition = new ComparisonCondition("age", ComparisonOperator.GT, "20");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> whereClause.or(comparisonCondition));

        assertEquals("WHERE 를 우선 지정해주세요.", exception.getMessage());
    }

    @Test
    void 조건이_없으면_빈문자열() {
        WhereClause whereClause = WhereClause.empty();

        assertEquals("", whereClause.toSql());
    }

    @Test
    void 복잡한조건조합이정확하게생성된다() {
        WhereClause whereClause = WhereClause.empty()
            .where(new ComparisonCondition("age", ComparisonOperator.GTE, "20"))
            .and(new ComparisonCondition("age", ComparisonOperator.LTE, "30"))
            .or(new ComparisonCondition("status", ComparisonOperator.EQ, "'ACTIVE'"))
            .and(new ComparisonCondition("role", ComparisonOperator.EQ, "'ADMIN'"));

        assertEquals(
            "WHERE (((age >= 20) AND (age <= 30)) OR (status = 'ACTIVE')) AND (role = 'ADMIN')",
            whereClause.toSql()
        );
    }

    @Test
    void WhereCondition_2개_and_결합() {
        WhereClause condition1 = WhereClause.empty()
            .where(new ComparisonCondition("age", ComparisonOperator.GTE, "20"))
            .and(new ComparisonCondition("age", ComparisonOperator.LTE, "30"));

        WhereClause condition2 = WhereClause.empty()
            .where(new ComparisonCondition("status", ComparisonOperator.EQ, "'ACTIVE'"))
            .and(new ComparisonCondition("role", ComparisonOperator.EQ, "'ADMIN'"));

        WhereClause result = condition1.and(condition2);

        assertEquals("WHERE ((age >= 20) AND (age <= 30)) AND ((status = 'ACTIVE') AND (role = 'ADMIN'))", result.toSql());
    }

    @Test
    void WhereCondition_2개_or_결합() {
        WhereClause condition1 = WhereClause.empty()
            .where(new ComparisonCondition("age", ComparisonOperator.GTE, "20"))
            .and(new ComparisonCondition("age", ComparisonOperator.LTE, "30"));

        WhereClause condition2 = WhereClause.empty()
            .where(new ComparisonCondition("status", ComparisonOperator.EQ, "'ACTIVE'"))
            .and(new ComparisonCondition("role", ComparisonOperator.EQ, "'ADMIN'"));

        WhereClause result = condition1.or(condition2);

        assertEquals("WHERE ((age >= 20) AND (age <= 30)) OR ((status = 'ACTIVE') AND (role = 'ADMIN'))", result.toSql());
    }
}
