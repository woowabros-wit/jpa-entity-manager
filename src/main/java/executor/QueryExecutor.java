package executor;

import builder.Query;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Pattern;
import mapper.ResultSetMapper;

public class QueryExecutor {

    private static final Pattern SQL_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private final Connection connection;
    private final ResultSetMapper mapper;

    public QueryExecutor(Connection connection) {
        this.connection = connection;
        this.mapper = new ResultSetMapper();
    }

    public <T> List<T> query(Query query, Class<T> resultClass) throws SQLException {
        String sql = query.build();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            return mapper.mapToList(resultSet, resultClass);
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public long maxPrimaryKeyPlusOne(String tableName, String idColumnName) throws SQLException {
        requireSqlIdentifier(tableName, "tableName");
        requireSqlIdentifier(idColumnName, "idColumnName");
        String sql =
            "SELECT COALESCE(MAX(" + idColumnName + "), 0) + 1 FROM " + tableName;
        try (Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)) {
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return 1L;
        }
    }

    private static void requireSqlIdentifier(String name, String role) {
        if (name == null || !SQL_IDENTIFIER.matcher(name).matches()) {
            throw new IllegalArgumentException(role + "은(는) 안전한 SQL 식별자여야 합니다: " + name);
        }
    }

    public int execute(Query query) throws SQLException {
        String sql = query.build();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return preparedStatement.executeUpdate();
        }
    }

    // TODO 효율적인 처리를 위해 배치 적용 필요. 하나만 실패하는 경우 고려되지 않은 상태라 수정 필요
    public void execute(List<Query> queries) throws SQLException {
        for (Query query : queries) {
            execute(query);
        }
    }
}
