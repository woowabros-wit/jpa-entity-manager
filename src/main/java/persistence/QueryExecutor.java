package persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class QueryExecutor {
    private final Connection connection;
    private final ResultSetMapper mapper;

    public QueryExecutor(Connection connection) {
        this.connection = connection;
        this.mapper = new ResultSetMapper();
    }

    /**
     * SELECT 쿼리 실행 및 자동 매핑
     */
    public <T> List<T> query(String sql, Class<T> resultClass, Object... params)
            throws Exception {
        try (var pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (var rs = pstmt.executeQuery()) {
                return mapper.mapToList(rs, resultClass);
            }
        }
    }

    /**
     * Named Parameter 쿼리 실행
     */
    public <T> List<T> query(NamedParameterQuery query, Class<T> resultClass)
            throws Exception {

        try (PreparedStatement pstmt = connection.prepareStatement(query.toJdbcSql())) {
            query.bindParameters(pstmt);  // 자동으로 파라미터 설정!

            try (ResultSet rs = pstmt.executeQuery()) {
                return mapper.mapToList(rs, resultClass);
            }
        }
    }

    /**
     * INSERT/UPDATE/DELETE 실행
     */
    public int execute(String sql, Object... params) throws SQLException {
        try (var pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt.executeUpdate();
        }
    }

    /**
     * Named Parameter로 실행
     */
    public int execute(NamedParameterQuery query) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(query.toJdbcSql())) {
            query.bindParameters(pstmt);
            return pstmt.executeUpdate();
        }
    }

    public void executeQuery(String sql) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.execute();
        }
    }
}