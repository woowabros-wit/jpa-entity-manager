package jdbc;

import jdbc.util.WhereClauseParser;
import jdbc.vo.CompareTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WhereClauseParserTest {

    @Test
    @DisplayName("등호_조건을_파싱한다")
    void test01() {
        List<CompareTarget> result = WhereClauseParser.parse("age = 10");

        assertEquals(1, result.size());
        assertEquals("age = 10", result.get(0).getTargetString());
        assertEquals("", result.get(0).getJoinString());
    }

    @Test
    @DisplayName("크거나_같다_조건을_파싱한다")
    void test02() {
        List<CompareTarget> result = WhereClauseParser.parse("age >= 10");

        assertEquals(1, result.size());
        assertEquals("age >= 10", result.get(0).getTargetString());
    }

    @Test
    @DisplayName("작거나_같다_조건을_파싱한다")
    void test03() {
        List<CompareTarget> result = WhereClauseParser.parse("age <= 10");

        assertEquals(1, result.size());
        assertEquals("age <= 10", result.get(0).getTargetString());
    }

    @Test
    @DisplayName("크다_조건을_파싱한다")
    void test04() {
        List<CompareTarget> result = WhereClauseParser.parse("age > 10");

        assertEquals(1, result.size());
        assertEquals("age > 10", result.get(0).getTargetString());
    }

    @Test
    @DisplayName("작다_조건을_파싱한다")
    void test05() {
        List<CompareTarget> result = WhereClauseParser.parse("age < 10");

        assertEquals(1, result.size());
        assertEquals("age < 10", result.get(0).getTargetString());
    }

    @Test
    @DisplayName("같지않다_조건을_파싱한다")
    void test06() {
        List<CompareTarget> result = WhereClauseParser.parse("status != active");

        assertEquals(1, result.size());
        assertEquals("status != active", result.get(0).getTargetString());
    }

    @Test
    @DisplayName("AND_조건으로_여러_비교를_파싱한다")
    void test07() {
        List<CompareTarget> result = WhereClauseParser.parse("age >= 10 AND name = kim");

        assertEquals(2, result.size());
        assertEquals("age >= 10", result.get(0).getTargetString());
        assertEquals(" AND ", result.get(0).getJoinString());
        assertEquals("name = kim", result.get(1).getTargetString());
        assertEquals(" AND ", result.get(1).getJoinString());
    }

    @Test
    @DisplayName("OR_조건으로_여러_비교를_파싱한다")
    void test08() {
        List<CompareTarget> result = WhereClauseParser.parse("age >= 10 OR name = kim");

        assertEquals(2, result.size());
        assertEquals("age >= 10", result.get(0).getTargetString());
        assertEquals(" OR ", result.get(0).getJoinString());
        assertEquals("name = kim", result.get(1).getTargetString());
        assertEquals(" OR ", result.get(1).getJoinString());
    }

    @Test
    @DisplayName("where로_시작하면_예외가_발생한다")
    void test09() {
        assertThrows(IllegalArgumentException.class,
                () -> WhereClauseParser.parse("where age = 10"));
    }

    @Test
    @DisplayName("AND와_OR가_동시에_있으면_예외가_발생한다")
    void test10() {
        assertThrows(IllegalArgumentException.class,
                () -> WhereClauseParser.parse("age >= 10 AND name = kim OR id = 1"));
    }

    @Test
    @DisplayName("named_parameter를_파싱한다")
    void test11() {
        List<CompareTarget> result = WhereClauseParser.parse("age = :age");

        assertEquals(1, result.size());
        assertEquals("age = ?", result.get(0).getTargetString());
        assertFalse(result.get(0).isNamedParameterEmpty());
        assertEquals("age", result.get(0).getNamedParameter());
    }

    @Test
    @DisplayName("물음표_플레이스홀더를_파싱한다")
    void test12() {
        List<CompareTarget> result = WhereClauseParser.parse("age = ?");

        assertEquals(1, result.size());
        assertEquals("age = ?", result.get(0).getTargetString());
        assertTrue(result.get(0).isNamedParameterEmpty());
    }

    @Test
    @DisplayName("세_개_이상의_AND_조건을_파싱한다")
    void test13() {
        List<CompareTarget> result = WhereClauseParser.parse("age >= 10 AND name = kim AND id = 1");

        assertEquals(3, result.size());
        assertEquals("age >= 10", result.get(0).getTargetString());
        assertEquals("name = kim", result.get(1).getTargetString());
        assertEquals("id = 1", result.get(2).getTargetString());
    }

    @Test
    @DisplayName("단일_조건에서_joinString은_빈_문자열이다")
    void test14() {
        List<CompareTarget> result = WhereClauseParser.parse("id = 1");

        assertEquals("", result.get(0).getJoinString());
    }

    @Test
    @DisplayName("비교_연산자가_없으면_예외가_발생한다")
    void test15() {
        assertThrows(IllegalArgumentException.class,
                () -> WhereClauseParser.parse("age 10"));
    }

    @Test
    @DisplayName("AND_조건에서_named_parameter가_있으면_물음표로_치환하여_파싱한다")
    void test16() {
        List<CompareTarget> result = WhereClauseParser.parse("age >= :minAge AND name = :name");

        assertEquals(2, result.size());
        assertEquals("age >= ?", result.get(0).getTargetString());
        assertEquals("minAge", result.get(0).getNamedParameter());
        assertEquals("name = ?", result.get(1).getTargetString());
        assertEquals("name", result.get(1).getNamedParameter());
    }
}
