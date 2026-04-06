package persistence;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NamedParameterQuery {

    private final String originalSql;
    private String jdbcSql;

    private final Map<String, Object> parameters = new HashMap<>();
    private final List<String> parms = new ArrayList<>();

    /**
     * Named Parameter를 지원하는 쿼리 생성
     *
     * @param originalSql Named Parameter가 포함된 SQL (:paramName 형식)
     */
    public NamedParameterQuery(String originalSql) {
        this.originalSql = originalSql;
        putParametersBy(originalSql);

    }

    void putParametersBy(String originalSql) {
        StringBuilder converted = new StringBuilder();
        int i = 0;
        while (i < originalSql.length()) {
            if (originalSql.charAt(i) == ':') {
                i++;
                StringBuilder paramName = new StringBuilder();
                while (i < originalSql.length() && Character.isLetterOrDigit(originalSql.charAt(i))) {
                    paramName.append(originalSql.charAt(i));
                    i++;
                }
                parms.add(paramName.toString());
                converted.append('?');
            } else {
                converted.append(originalSql.charAt(i));
                i++;
            }
        }
        this.jdbcSql = converted.toString();
    }


    /**
     * 파라미터 값 설정
     *
     * @param name  파라미터 이름
     * @param value 파라미터 값
     */
    public NamedParameterQuery setParameter(String name, Object value) {
        if (getParms().contains(name) == false) {
            throw new IllegalArgumentException("잘못된 파라미터 이름: " + name);
        }

        parameters.put(name, value);
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
        Map<String, Object> parameters1 = getParameters();
        List<String> parms1 = getParms();

        for (int i = 0; i < parms1.size(); i++) {
            String paramKey = parms1.get(i);

            Object paramValue = parameters1.get(paramKey);
            if (paramValue == null) {
                throw new IllegalArgumentException("파라미터 값 설정 없음" + " key: " + paramKey);
            }
            pstmt.setObject(i + 1, paramValue);
        }
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public List<String> getParms() {
        return parms;
    }
}
