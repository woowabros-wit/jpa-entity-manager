package jdbc.util;

import jdbc.vo.ReserveWord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReserveWordTokenizerTest {

    @Test
    @DisplayName("SELECT_FROM_두_예약어_사이의_토큰을_추출한다")
    void test01() {
        List<ReserveWord> reserveWords = List.of(
                new ReserveWord("SELECT", true, 1),
                new ReserveWord("FROM", true, 2)
        );

        ReserveWordTokenizer tokenizer = new ReserveWordTokenizer(reserveWords, "SELECT id, name FROM users");
        Map<String, String> tokens = tokenizer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals("id,name", tokens.get("SELECT"));
        assertEquals("users", tokens.get("FROM"));
    }

    @Test
    @DisplayName("SELECT_FROM_WHERE_세_예약어로_토큰을_추출한다")
    void test02() {
        List<ReserveWord> reserveWords = List.of(
                new ReserveWord("SELECT", true, 1),
                new ReserveWord("FROM", true, 2),
                new ReserveWord("WHERE", true, 3)
        );

        ReserveWordTokenizer tokenizer = new ReserveWordTokenizer(reserveWords, "SELECT * FROM users WHERE age > 10");
        Map<String, String> tokens = tokenizer.tokenize();

        assertEquals(3, tokens.size());
        assertEquals("*", tokens.get("SELECT"));
        assertEquals("users", tokens.get("FROM"));
        assertEquals("age > 10", tokens.get("WHERE"));
    }

    @Test
    @DisplayName("소문자_SQL도_정상적으로_토큰화한다")
    void test03() {
        List<ReserveWord> reserveWords = List.of(
                new ReserveWord("SELECT", true, 1),
                new ReserveWord("FROM", true, 2)
        );

        ReserveWordTokenizer tokenizer = new ReserveWordTokenizer(reserveWords, "select id from users");
        Map<String, String> tokens = tokenizer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals("id", tokens.get("SELECT"));
        assertEquals("users", tokens.get("FROM"));
    }

    @Test
    @DisplayName("여러_공백이_있어도_정상적으로_토큰화한다")
    void test04() {
        List<ReserveWord> reserveWords = List.of(
                new ReserveWord("SELECT", true, 1),
                new ReserveWord("FROM", true, 2)
        );

        ReserveWordTokenizer tokenizer = new ReserveWordTokenizer(reserveWords, "SELECT   id ,  name   FROM   users");
        Map<String, String> tokens = tokenizer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals("id,name", tokens.get("SELECT"));
        assertEquals("users", tokens.get("FROM"));
    }

    @Test
    @DisplayName("필수_예약어가_없으면_예외가_발생한다")
    void test05() {
        List<ReserveWord> reserveWords = List.of(
                new ReserveWord("SELECT", true, 1),
                new ReserveWord("FROM", true, 2)
        );

        ReserveWordTokenizer tokenizer = new ReserveWordTokenizer(reserveWords, "INSERT INTO users VALUES (1)");

        assertThrows(IllegalArgumentException.class, tokenizer::tokenize);
    }

    @Test
    @DisplayName("예약어가_2개_이상_존재하면_예외가_발생한다")
    void test06() {
        List<ReserveWord> reserveWords = List.of(
                new ReserveWord("SELECT", true, 1),
                new ReserveWord("FROM", true, 2)
        );

        ReserveWordTokenizer tokenizer = new ReserveWordTokenizer(reserveWords, "SELECT id FROM users FROM orders");

        assertThrows(IllegalArgumentException.class, tokenizer::tokenize);
    }

    @Test
    @DisplayName("SQL이_예약어로_끝나면_예외가_발생한다")
    void test07() {
        List<ReserveWord> reserveWords = List.of(
                new ReserveWord("SELECT", true, 1),
                new ReserveWord("FROM", true, 2),
                new ReserveWord("WHERE", true, 3)
        );

        ReserveWordTokenizer tokenizer = new ReserveWordTokenizer(reserveWords, "SELECT id FROM users WHERE");

        assertThrows(IllegalArgumentException.class, tokenizer::tokenize);
    }

    @Test
    @DisplayName("ORDER_BY_LIMIT을_포함한_복합_쿼리를_토큰화한다")
    void test08() {
        List<ReserveWord> reserveWords = List.of(
                new ReserveWord("SELECT", true, 1),
                new ReserveWord("FROM", true, 2),
                new ReserveWord("WHERE", true, 3),
                new ReserveWord("ORDER BY", true, 4),
                new ReserveWord("LIMIT", true, 5)
        );

        ReserveWordTokenizer tokenizer = new ReserveWordTokenizer(reserveWords,
                "SELECT * FROM users WHERE age >= 20 ORDER BY name ASC LIMIT 10");
        Map<String, String> tokens = tokenizer.tokenize();

        assertEquals(5, tokens.size());
        assertEquals("*", tokens.get("SELECT"));
        assertEquals("users", tokens.get("FROM"));
        assertEquals("age >= 20", tokens.get("WHERE"));
        assertEquals("name ASC", tokens.get("ORDER BY"));
        assertEquals("10", tokens.get("LIMIT"));
    }

    @Test
    @DisplayName("비필수_예약어가_없어도_정상_동작한다")
    void test09() {
        List<ReserveWord> reserveWords = List.of(
                new ReserveWord("SELECT", true, 1),
                new ReserveWord("FROM", true, 2),
                new ReserveWord("WHERE", false, 3)
        );

        ReserveWordTokenizer tokenizer = new ReserveWordTokenizer(reserveWords, "SELECT id FROM users");
        Map<String, String> tokens = tokenizer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals("id", tokens.get("SELECT"));
        assertEquals("users", tokens.get("FROM"));
        assertNull(tokens.get("WHERE"));
    }

    @Test
    @DisplayName("예약어_사이에_빈값이_있으면_예외가_발생한다")
    void test10() {
        List<ReserveWord> reserveWords = List.of(
                new ReserveWord("SELECT", true, 1),
                new ReserveWord("FROM", true, 2)
        );

        ReserveWordTokenizer tokenizer = new ReserveWordTokenizer(reserveWords, "SELECT FROM users");

        assertThrows(IllegalArgumentException.class, tokenizer::tokenize);
    }

    @Test
    @DisplayName("비필수_예약어가_존재할_때도_정상_토큰화한다")
    void test11() {
        List<ReserveWord> reserveWords = List.of(
                new ReserveWord("SELECT", true, 1),
                new ReserveWord("FROM", true, 2),
                new ReserveWord("WHERE", false, 3)
        );

        ReserveWordTokenizer tokenizer = new ReserveWordTokenizer(reserveWords, "SELECT id FROM users WHERE age > 10");
        Map<String, String> tokens = tokenizer.tokenize();

        assertEquals(3, tokens.size());
        assertEquals("id", tokens.get("SELECT"));
        assertEquals("users", tokens.get("FROM"));
        assertEquals("age > 10", tokens.get("WHERE"));
    }

    @Test
    @DisplayName("예약어가_하나만_있을_때_뒤의_토큰을_추출한다")
    void test12() {
        List<ReserveWord> reserveWords = List.of(
                new ReserveWord("INSERT INTO", true, 1)
        );

        ReserveWordTokenizer tokenizer = new ReserveWordTokenizer(reserveWords, "INSERT INTO users");
        Map<String, String> tokens = tokenizer.tokenize();

        assertEquals(1, tokens.size());
        assertEquals("users", tokens.get("INSERT INTO"));
    }

    @Test
    @DisplayName("컬럼명에_예약어가_부분_문자열로_포함되어도_정상_토큰화한다")
    void test13() {
        List<ReserveWord> reserveWords = List.of(
                new ReserveWord("SELECT", true, 1),
                new ReserveWord("FROM", true, 2)
        );

        ReserveWordTokenizer tokenizer = new ReserveWordTokenizer(reserveWords, "SELECT from_date FROM users");
        Map<String, String> tokens = tokenizer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals("from_date", tokens.get("SELECT"));
        assertEquals("users", tokens.get("FROM"));
    }

    @Test
    @DisplayName("예약어_순서가_SQL_등장_순서와_다르면_정상_토큰화한다")
    void test14() {
        List<ReserveWord> reserveWords = List.of(
                new ReserveWord("FROM", true, 2),
                new ReserveWord("SELECT", true, 1)
        );

        ReserveWordTokenizer tokenizer = new ReserveWordTokenizer(reserveWords, "SELECT id FROM users");
        Map<String, String> tokens = tokenizer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals("id", tokens.get("SELECT"));
        assertEquals("users", tokens.get("FROM"));
    }

    @Test
    @DisplayName("UPDATE_SET_WHERE_예약어로_토큰화한다")
    void test15() {
        List<ReserveWord> reserveWords = List.of(
                new ReserveWord("UPDATE", true, 1),
                new ReserveWord("SET", true, 2),
                new ReserveWord("WHERE", true, 3)
        );

        ReserveWordTokenizer tokenizer = new ReserveWordTokenizer(reserveWords, "UPDATE users SET name = ? WHERE id = 1");
        Map<String, String> tokens = tokenizer.tokenize();

        assertEquals(3, tokens.size());
        assertEquals("users", tokens.get("UPDATE"));
        assertEquals("name = ?", tokens.get("SET"));
        assertEquals("id = 1", tokens.get("WHERE"));
    }

    @Test
    @DisplayName("DELETE_FROM_WHERE_예약어로_토큰화한다")
    void test16() {
        List<ReserveWord> reserveWords = List.of(
                new ReserveWord("DELETE FROM", true, 1),
                new ReserveWord("WHERE", true, 2)
        );

        ReserveWordTokenizer tokenizer = new ReserveWordTokenizer(reserveWords, "DELETE FROM users WHERE id = 1");
        Map<String, String> tokens = tokenizer.tokenize();

        assertEquals(2, tokens.size());
        assertEquals("users", tokens.get("DELETE FROM"));
        assertEquals("id = 1", tokens.get("WHERE"));
    }
}
