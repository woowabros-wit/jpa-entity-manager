package query;

import org.junit.jupiter.api.Test;
import persistence.query.InsertQueryBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InsertQueryBuilderTest {

    @Test
    void INSERT_쿼리_생성_3개_컬럼() {
        String sql = new InsertQueryBuilder()
                .into("users")
                .value("name", "?")
                .value("age", "?")
                .value("email", "?")
                .build();

        assertEquals("INSERT INTO users (name, age, email) VALUES (?, ?, ?)", sql);
    }

    @Test
    void INSERT_쿼리_생성_Map_사용() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("name", "?");
        values.put("age", "?");

        String sql = new InsertQueryBuilder()
                .into("users")
                .values(values)
                .build();

        assertEquals("INSERT INTO users (name, age) VALUES (?, ?)", sql);
    }

    @Test
    void INSERT_쿼리_컬럼_순서_보장() {
        String sql = new InsertQueryBuilder()
                .into("users")
                .value("id", "?")
                .value("name", "?")
                .value("age", "?")
                .value("email", "?")
                .value("status", "?")
                .build();

        assertEquals("INSERT INTO users (id, name, age, email, status) VALUES (?, ?, ?, ?, ?)", sql);
    }

    @Test
    void INSERT_쿼리_테이블명_없으면_예외() {
        assertThrows(IllegalStateException.class, () -> {
            new InsertQueryBuilder()
                    .value("name", "?")
                    .build();
        });
    }

    @Test
    void INSERT_쿼리_컬럼_없으면_예외() {
        assertThrows(IllegalStateException.class, () -> {
            new InsertQueryBuilder()
                    .into("users")
                    .build();
        });
    }
}