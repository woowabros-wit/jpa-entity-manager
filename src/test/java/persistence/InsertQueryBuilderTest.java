package persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InsertQueryBuilderTest {

    @Test
    void basic() {

        String sql = new InsertQueryBuilder()
                .into("users")
                .value("name", "?")
                .value("age", "?")
                .value("email", "?")
                .build();

        assertThat(sql).isEqualTo("INSERT INTO users (name, age, email) VALUES (?, ?, ?)");
    }

    @Test
    void basic_with_map() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("name", "?");
        values.put("age", "?");

        String sql = new InsertQueryBuilder()
                .into("users")
                .values(values)
                .build();

        assertThat(sql).isEqualTo("INSERT INTO users (name, age) VALUES (?, ?)");
    }

    @Test
    @DisplayName("테이블 설정이 없다면 에러반환")
    void test() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("name", "?");
        values.put("age", "?");


        assertThatThrownBy(() -> new InsertQueryBuilder()
                .values(values)
                .build()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("")
    void name() {

    }
}