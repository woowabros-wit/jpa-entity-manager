package persistence;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamedParameterQuery {
    private static final Pattern NAMED_PARAM_PATTERN = Pattern.compile(":([a-zA-Z][a-zA-Z0-9_]*)");

    private final String jdbcSql;
    private final List<String> parameterOrder;
    private final Map<String, Object> parameterValues;

    /**
     * Named Parameter를 지원하는 쿼리 생성
     *
     * @param sql Named Parameter가 포함된 SQL (:paramName 형식)
     */
    public NamedParameterQuery(String sql) {
        this.parameterOrder = new ArrayList<>();
        this.parameterValues = new HashMap<>();
        this.jdbcSql = parseSql(sql);
    }
    
    private String parseSql(String sql) {
        validateUnsupportedParameterFormats(sql);
        Matcher matcher = NAMED_PARAM_PATTERN.matcher(sql);

        StringBuilder jdbcSqlBuilder = new StringBuilder();

        while (matcher.find()) {
            String paramName = matcher.group(1);
            parameterOrder.add(paramName);
            matcher.appendReplacement(jdbcSqlBuilder, "?");
        }
        matcher.appendTail(jdbcSqlBuilder);

        return jdbcSqlBuilder.toString();
    }


    /**
     * 파라미터 값 설정
     *
     * @param name  파라미터 이름
     * @param value 파라미터 값
     */
    public NamedParameterQuery setParameter(String name, Object value) {
        parameterValues.put(name, value);
        return this;
    }

    /**
     * JDBC용 SQL로 변환 (? 플레이스홀더)
     *
     * @return 변환된 SQL
     */
    public String toJdbcSql() {
        return jdbcSql;
    }

    /**
     * PreparedStatement에 파라미터 바인딩
     *
     * @param pstmt PreparedStatement
     */
    public void bindParameters(PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < parameterOrder.size(); i++) {
            String paramName = parameterOrder.get(i);
            if (!parameterValues.containsKey(paramName)) {
                throw new IllegalStateException("파라미터 '" + paramName + "'에 대한 값이 설정되지 않았습니다.");
            }
            pstmt.setObject(i + 1, parameterValues.get(paramName));
        }
    }

    private void validateUnsupportedParameterFormats(String sql) {
        Pattern unsupportedDollar = Pattern.compile("\\$\\{[a-zA-Z][a-zA-Z0-9_]*}");
        Pattern unsupportedAt = Pattern.compile("@([a-zA-Z][a-zA-Z0-9_]*)");

        if (unsupportedDollar.matcher(sql).find()) {
            throw new IllegalArgumentException(
                    "지원하지 않는 파라미터 형식입니다. ${paramName} 형식은 지원하지 않습니다. :paramName 형식을 사용하세요.");
        }
        if (unsupportedAt.matcher(sql).find()) {
            throw new IllegalArgumentException(
                    "지원하지 않는 파라미터 형식입니다. @paramName 형식은 지원하지 않습니다. :paramName 형식을 사용하세요.");
        }
    }
}
