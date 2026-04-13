package persistence.query;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamedParameterQuery {
    private static final Pattern NAMED_PARAMETER_REGEX_PATTERN = Pattern.compile(":([a-zA-Z0-9_]+)");

    private String sql;
    private Map<String, Object> params;
    private List<String> parameterNames;

    public NamedParameterQuery(String sql) {
        this.sql = sql;
        this.params = new HashMap<>();
        this.parameterNames = new ArrayList<>();
    }

    public NamedParameterQuery setParameter(String name, Object value) {
        params.put(name, value);
        return this;
    }

    public String toJdbcSql() {
        parameterNames.clear();
        Matcher matcher = NAMED_PARAMETER_REGEX_PATTERN.matcher(sql);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String paramName = matcher.group(1);
            parameterNames.add(paramName);
            matcher.appendReplacement(result, "?");
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public void bindParameters(PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < parameterNames.size(); i++) {
            String paramName = parameterNames.get(i);
            Object value = params.get(paramName);

            if (value == null) {
                throw new IllegalArgumentException();
            }

            if (value instanceof String) {
                pstmt.setString(i + 1, (String) value);
            } else if (value instanceof Integer) {
                pstmt.setInt(i + 1, (Integer) value);
            } else if (value instanceof Long) {
                pstmt.setLong(i + 1, (Long) value);
            } else if (value instanceof Boolean) {
                pstmt.setBoolean(i + 1, (Boolean) value);
            } else if (value instanceof Double) {
                pstmt.setDouble(i + 1, (Double) value);
            } else {
                pstmt.setObject(i + 1, value);
            }
        }
    }
}