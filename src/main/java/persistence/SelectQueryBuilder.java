package persistence;

public class SelectQueryBuilder {

    private String[] columns;
    private String table;
    private String orderByColumn;
    private OrderDirection orderByDirection;
    private Integer limit;
    private String whereCondition;

    /**
     * SELECT 절 지정
     *
     * @param columns 컬럼명 (가변 인자)
     * @return this (메서드 체이닝)
     */
    public SelectQueryBuilder select(String... columns) {
        this.columns = columns;

        return this;
    }

    /**
     * FROM 절 지정
     *
     * @param table 테이블명
     * @return this
     */
    public SelectQueryBuilder from(String table) {
        this.table = table;
        return this;
    }

    /**
     * ORDER BY 절 지정
     *
     * @param column    정렬 컬럼
     * @param direction "ASC" 또는 "DESC"
     * @return this
     */
    public SelectQueryBuilder orderBy(String column, OrderDirection direction) {
        this.orderByColumn = column;
        this.orderByDirection = direction;
        return this;
    }

    /**
     * LIMIT 절 지정
     *
     * @param limit 조회 개수
     * @return this
     */
    public SelectQueryBuilder limit(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("LIMIT 값은 1 이상이어야 합니다.");
        }
        this.limit = limit;
        return this;
    }

    // SELECT 쿼리에 WHERE 조건을 추가하여 특정 조건의 데이터만 조회할 수 있다.
    public SelectQueryBuilder where(String condition) {
        this.whereCondition = condition;
        return this;
    }

    /**
     * SQL 문자열 생성
     *
     * @return 생성된 SQL
     */
    public String build() {
        if (table == null) {
            throw new IllegalStateException("FROM 절이 지정되지 않았습니다.");
        }
        if (limit != null && limit <= 0) {
            throw new IllegalArgumentException("LIMIT 값은 1 이상이어야 합니다.");
        }

        return "SELECT " + (columns != null ? String.join(", ", columns) : "*") +
                " FROM " + table +
                (whereCondition != null ? " WHERE " + whereCondition : "") +
                (orderByColumn != null ? " ORDER BY " + orderByColumn + " " + orderByDirection : "") +
                (limit != null ? " LIMIT " + limit : "");
    }

    enum OrderDirection {
        ASC, DESC
    }
}
