package executor;

import builder.Query;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import mapper.ResultSetMapper;

public class QueryExecutor {

    private final Connection connection;
    private final ResultSetMapper mapper;

    public QueryExecutor(Connection connection) {
        this.connection = connection;
        this.mapper = new ResultSetMapper();
    }

    public <T> List<T> query(Query query, Class<T> resultClass) throws SQLException {
        String sql = query.build();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        try {
            return mapper.mapToList(resultSet, resultClass);
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public int execute(Query query) throws SQLException {
        String sql = query.build();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        return preparedStatement.executeUpdate();
    }
}
