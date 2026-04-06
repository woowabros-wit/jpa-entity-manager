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
        PreparedStatement pstm = connection.prepareStatement(sql);

        for (int i = 0; i < params.length; i++) {
            pstm.setObject(i + 1, params[i]);
        }

        ResultSet resultSet = pstm.executeQuery();
        return mapper.mapToList(resultSet, resultClass);
    }

    ;

    /**
     * Named Parameter 쿼리 실행
     */
    public <T> List<T> query(NamedParameterQuery query, Class<T> resultClass)
            throws Exception {
        PreparedStatement pstm = connection.prepareStatement(query.toJdbcSql());

        query.bindParameters(pstm);
        ResultSet resultSet = pstm.executeQuery();
        return mapper.mapToList(resultSet, resultClass);
    }

    /**
     * INSERT/UPDATE/DELETE 실행
     */
    public int execute(String sql, Object... params) throws SQLException {
        PreparedStatement pstm = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            pstm.setObject(i + 1, params[i]);
        }
        return pstm.executeUpdate();
    }

    /**
     * Named Parameter로 실행
     */
    public int execute(NamedParameterQuery query) throws SQLException {
        PreparedStatement pstm = connection.prepareStatement(query.toJdbcSql());
        query.bindParameters(pstm);
        return pstm.executeUpdate();
    }

    public void executeQuery(String s) {

    }
}
