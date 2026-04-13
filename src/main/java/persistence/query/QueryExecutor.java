package persistence.query;

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

    public ResultSet executeQuery(String sql) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(sql);
        return pstmt.executeQuery();
    }

    public <T> List<T> query(String sql, Class<T> resultClass, Object... params) throws Exception {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            bindParameters(pstmt, params);

            try (ResultSet rs = pstmt.executeQuery()) {
                return mapper.mapToList(rs, resultClass);
            }
        }
    }

    public <T> List<T> query(NamedParameterQuery query, Class<T> resultClass) throws Exception {
        String jdbcSql = query.toJdbcSql();

        try (PreparedStatement pstmt = connection.prepareStatement(jdbcSql)) {
            query.bindParameters(pstmt);

            try (ResultSet rs = pstmt.executeQuery()) {
                return mapper.mapToList(rs, resultClass);
            }
        }
    }

    public int execute(String sql, Object... params) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            bindParameters(pstmt, params);
            return pstmt.executeUpdate();
        }
    }

    public int execute(NamedParameterQuery query) throws Exception {
        String jdbcSql = query.toJdbcSql();

        try (PreparedStatement pstmt = connection.prepareStatement(jdbcSql)) {
            query.bindParameters(pstmt);
            return pstmt.executeUpdate();
        }
    }

    private void bindParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];

            if (param == null) {
                pstmt.setObject(i + 1, null);
            } else if (param instanceof String) {
                pstmt.setString(i + 1, (String) param);
            } else if (param instanceof Integer) {
                pstmt.setInt(i + 1, (Integer) param);
            } else if (param instanceof Long) {
                pstmt.setLong(i + 1, (Long) param);
            } else if (param instanceof Boolean) {
                pstmt.setBoolean(i + 1, (Boolean) param);
            } else if (param instanceof Double) {
                pstmt.setDouble(i + 1, (Double) param);
            } else {
                pstmt.setObject(i + 1, param);
            }
        }
    }
}
