package persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InsertQueryBuilderTest {
    /**
     * INSERT 쿼리를 생성하여 새로운 데이터를 데이터베이스에 추가할 수 있다.
     * e.g. INSERT INTO users (name, age, email) VALUES (?, ?, ?)
     * <p>
     * INSERT 쿼리는 최소 1개 이상의 컬럼-값 쌍을 가져야 하며, 없으면 예외를 발생시킨다.
     * <p>
     * 모든 쿼리에서 파라미터 플레이스홀더(?)를 사용하여 SQL 인젝션을 방지한다.
     */


    @Test
    @DisplayName("INSERT 쿼리를 생성하여 새로운 데이터를 데이터베이스에 추가할 수 있다.")
    void case1() {
        String sql = new InsertQueryBuilder()
                .into("users")
                .value("name", "?")
                .value("age", "?")
                .value("email", "?")
                .build();

        assertThat(sql).isEqualTo("INSERT INTO users (name, age, email) VALUES (?, ?, ?)");
    }


    @Test
    @DisplayName("컬럼과 값을 하나씩 추가하거나, Map으로 한 번에 추가할 수 있다.")
    void case2() {
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
    @DisplayName("INSERT 쿼리는 최소 1개 이상의 컬럼-값 쌍을 가져야 하며, 없으면 예외를 발생시킨다.")
    void case3() {
        assertThatThrownBy(() -> new InsertQueryBuilder()
                .into("users")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최소 1개 이상의 컬럼-값 쌍을 가져야 함");
        ;
    }

    @Test
    @DisplayName("INSERT/UPDATE 시 컬럼 순서가 추가한 순서대로 유지된다.")
    void case4() {
        assertThat(new InsertQueryBuilder()
                .into("users")
                .value("age", "?")
                .value("name", "?")
                .build()).isEqualTo("INSERT INTO users (age, name) VALUES (?, ?)");


        assertThat(new InsertQueryBuilder()
                .into("users")
                .value("name", "?")
                .value("age", "?")
                .build()).isEqualTo("INSERT INTO users (name, age) VALUES (?, ?)");
    }
}