package builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
class InsertQueryBuilderTest {

    @Test
    void VALUE를_활용해_INSERT_쿼리_생성() {
        String sql = new InsertQueryBuilder()
            .into("users")
            .value("name", "?")
            .value("age", "?")
            .build();

        assertEquals("INSERT INTO users (name, age) VALUES (?, ?)", sql);
    }

    @Test
    void VALUES를_활용해_INSERT_쿼리_생성() {
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
    void INTO_없이_빌드하면_예외_발생() {
        InsertQueryBuilder builder = new InsertQueryBuilder()
            .value("name", "?");

        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void VALUE_없이_빌드하면_예외_발생() {
        InsertQueryBuilder builder = new InsertQueryBuilder()
            .into("users");

        assertThrows(IllegalStateException.class, builder::build);
    }
}
