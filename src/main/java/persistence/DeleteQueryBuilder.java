package persistence;

public class DeleteQueryBuilder {

    private final StringBuilder query = new StringBuilder("DELETE ");
    private String from = null;
    private String where = null;

    /**
     * DELETE할 테이블 지정
     */
    public DeleteQueryBuilder from(String table) {
        from = table;

        return this;
    }

    /**
     * WHERE 절 추가
     */
    public DeleteQueryBuilder where(String condition) {
        where = condition;

        return this;
    }

    /**
     * SQL 생성
     */
    public String build() {
        if (!isValidQuery(where)) {
            throw new IllegalStateException();
        }

        return query.append("FROM ")
                .append(from)
                .append(" WHERE ")
                .append(where)
                .toString();
    }

    private boolean isValidQuery(String where) {
        return where != null && !where.isBlank();
    }
}
