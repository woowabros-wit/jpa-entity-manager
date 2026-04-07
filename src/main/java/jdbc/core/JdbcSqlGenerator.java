package jdbc.core;

public interface JdbcSqlGenerator extends QueryManager {

    String makeJdbcQuery(String sql);
}
