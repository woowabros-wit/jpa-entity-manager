package persistence;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SelectQueryBuilder {

    private String select = null;
    private String from = null;
    private String orderBy = null;
    private String limit = null;
    private String where = null;

    /**
     * SELECT 절 지정
     * @param columns 컬럼명 (가변 인자)
     * @return this (메서드 체이닝)
     */
    public SelectQueryBuilder select(String... columns) {
        if (columns == null || columns.length == 0) {
            select = "*";
            return this;
        }

        select = Arrays.stream(columns).collect(Collectors.joining(", "));
        return this;
    }

    /**
     * FROM 절 지정
     * @param table 테이블명
     * @return this
     */
    public SelectQueryBuilder from(String table) {
        from = " FROM " + table;
        return this;
    }

    /**
     * ORDER BY 절 지정
     * @param column 정렬 컬럼
     * @param direction "ASC" 또는 "DESC"
     * @return this
     */
    public SelectQueryBuilder orderBy(String column, String direction) {
        orderBy = " ORDER BY " + column + " " + direction;
        return this;
    }

    /**
     * LIMIT 절 지정
     * @param limit 조회 개수
     * @return this
     */
    public SelectQueryBuilder limit(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("양의 정수");
        }

        this.limit = " LIMIT " + limit;
        return this;
    }

    public SelectQueryBuilder where(String condition) {
        this.where = condition;

        return this;
    }

    /**
     * SQL 문자열 생성
     * @return 생성된 SQL
     */
    public String build() {
        StringBuilder query = new StringBuilder("SELECT ");

        if (from == null) {
            throw new IllegalStateException("FROM 절이 지정되지 않았습니다.");
        }

        if (select != null) {
            query.append(select);
        }

        if (from != null) {
            query.append(from);
        }

        if (where != null) {
            query.append(" WHERE ").append(where);
        }

        if (orderBy != null) {
            query.append(orderBy);
        }

        if (limit != null) {
            query.append(limit);
        }

        return query.toString();
    }
}
