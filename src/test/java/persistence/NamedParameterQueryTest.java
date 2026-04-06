package persistence;

import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NamedParameterQueryTest {

    @Test
    void OriginalQuery의_변수를_파싱한다() {
        String sql = "SELECT * FROM users WHERE age >= :minAge AND name LIKE :namePattern";
        NamedParameterQuery namedParameterQuery = new NamedParameterQuery(sql);
        List<String> parms = namedParameterQuery.getParms();
        assertThat(parms).hasSize(2);
        assertThat(parms.get(0)).isEqualTo("minAge");
        assertThat(parms.get(1)).isEqualTo("namePattern");
    }

    @Test
    void Named_Parameter를_JDBC_플레이스홀더로_변환() {
        String sql = "SELECT * FROM users WHERE age >= :minAge AND name LIKE :namePattern";

        NamedParameterQuery query = new NamedParameterQuery(sql);

        String jdbcSql = query.toJdbcSql();
        assertThat(jdbcSql).isEqualTo("SELECT * FROM users WHERE age >= ? AND name LIKE ?");
    }

    @Test
    void 바인딩된_Parameters를_확인_할_수_있다() {
        String sql = "SELECT * FROM users WHERE age >= :minAge AND name LIKE :namePattern";

        NamedParameterQuery query = new NamedParameterQuery(sql);

        String jdbcSql = query.toJdbcSql();
        query.setParameter("minAge", 20);
        query.setParameter("namePattern", "%test%");

        assertThat(jdbcSql).isEqualTo("SELECT * FROM users WHERE age >= ? AND name LIKE ?");

        Map<String, Object> parameters = query.getParameters();
        assertThat(parameters).containsEntry("minAge", 20);
        assertThat(parameters).containsEntry("namePattern", "%test%");
    }

    @Test
    void 유효하지_않는_Parameter_Name으로__set_할_수_없다() {
        String sql = "SELECT * FROM users WHERE age >= :minAge AND name LIKE :namePattern";

        NamedParameterQuery query = new NamedParameterQuery(sql);

        assertThatThrownBy(() -> query.setParameter("noName", 20))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("잘못된 파라미터");
    }

    @Test
    void Named_Parameter에서_값이_없는_value_를_바인딩_할_수_없다() throws SQLException {
        String sql = "SELECT * FROM users WHERE age >= :min";
        PreparedStatement pstmt = mock(PreparedStatement.class);

        NamedParameterQuery query = new NamedParameterQuery(sql);
        query.setParameter("min", null);

        assertThatThrownBy(() -> query.bindParameters(pstmt)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("파라미터 값 설정 없음");
    }

    @Test
    void Named_Parameter를_PreparedStatement_바인딩_할_수_있다() throws SQLException {
        String sql = "SELECT * FROM users WHERE age >= :min";
        PreparedStatement pstmt = mock(PreparedStatement.class);

        NamedParameterQuery query = new NamedParameterQuery(sql);
        query.setParameter("min", 20);

        query.bindParameters(pstmt);

        verify(pstmt).setObject(1, 20);
    }


    @Test
    void Named_Parameter가_여러번_사용되면_각_위치에_동일한_값을_바인딩() throws SQLException {
        String sql = "SELECT * FROM users WHERE age >= :min AND height >= :min";

        NamedParameterQuery query = new NamedParameterQuery(sql);
        String jdbcSql = query.toJdbcSql();
        assertThat(jdbcSql).isEqualTo("SELECT * FROM users WHERE age >= ? AND height >= ?");
        PreparedStatement pstmt = mock(PreparedStatement.class);
        query.setParameter("min", 20);

        query.bindParameters(pstmt);

        verify(pstmt).setObject(1, 20);
        verify(pstmt).setObject(2, 20);
    }
}