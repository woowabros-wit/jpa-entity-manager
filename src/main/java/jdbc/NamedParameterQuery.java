package jdbc;

import jdbc.core.*;
import jdbc.vo.NamedParameterBindCommand;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class NamedParameterQuery {

    private final String targetSql;
    private final QueryManager queryManager;
    private final String jdbcQuery;


    /**
     * Named Parameter를 지원하는 쿼리 생성
     * @param sql Named Parameter가 포함된 SQL (:paramName 형식)
     */
    public NamedParameterQuery(String sql) {
        this.targetSql = sql;

        List<QueryManager> queryManagers = List.of(
                new SelectQueryBuilder(),
                new InsertQueryBuilder(),
                new DeleteQueryBuilder(),
                new UpdateQueryBuilder()
        );
        this.queryManager = queryManagers
                .stream()
                .filter(it -> it.isSupported(sql))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하는 query builder 없음"));

        this.jdbcQuery = ((JdbcSqlGenerator) this.queryManager).makeJdbcQuery(this.targetSql);
    }

    /**
     * 파라미터 값 설정
     * @param name 파라미터 이름
     * @param value 파라미터 값
     */
    public NamedParameterQuery setParameter(String name, Object value) {
        ((NamedParameterTargetProcessor) this.queryManager).processParameter(name, value);
        return this;
    }

    /**
     * JDBC용 SQL로 변환 (? 플레이스홀더)
     * @return 변환된 SQL
     */
    public String toJdbcSql() {
        return this.jdbcQuery;
    }

    /**
     * PreparedStatement에 파라미터 바인딩
     * @param pstmt PreparedStatement
     */
    public void bindParameters(PreparedStatement pstmt) throws SQLException {
        List<NamedParameterBindCommand> command = ((NamedParameterTargetProcessor) this.queryManager).getCommand();
        command.forEach(it -> {
            try {
                pstmt.setObject(it.getOrder(), it.getValue());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
