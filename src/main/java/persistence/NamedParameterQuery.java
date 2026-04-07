package persistence;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NamedParameterQuery {

    private final String sql;
    private final Map<String, Object> parameters = new LinkedHashMap<>();
    private final List<String> namesInSqlOrder = new ArrayList<>();

    public NamedParameterQuery(String sql) {
        this.sql = sql;
    }

    public NamedParameterQuery setParameter(String name, Object value) {
        parameters.put(name, value);
        return this;
    }

    public String toJdbcSql() {
        StringBuilder jdbc = new StringBuilder();
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == ':' && i + 1 < sql.length()) {
                int j = i + 1;
                while (j < sql.length() && sql.charAt(j) != ' ') {
                    j++;
                }
                namesInSqlOrder.add(sql.substring(i + 1, j));
                jdbc.append('?');
                i = j - 1;
            } else {
                jdbc.append(c);
            }
        }

        return jdbc.toString();
    }

    public void bindParameters(PreparedStatement pstmt) throws SQLException {
        int index = 1;
        for (String name : namesInSqlOrder) {
            pstmt.setObject(index++, parameters.get(name));
        }
    }
}
