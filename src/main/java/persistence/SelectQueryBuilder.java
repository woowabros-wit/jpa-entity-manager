package persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectQueryBuilder {

    List<String> columns = new ArrayList<>();
    List<String> froms = new ArrayList<>();
    List<String> orders = new ArrayList<>();
    List<String> limits = new ArrayList<>();

    /**
     * SELECT 절 지정
     *
     * @param columns 컬럼명 (가변 인자)
     * @return this (메서드 체이닝)
     */
    public SelectQueryBuilder select(String... columns) {
        this.columns.addAll(Arrays.asList(columns));
        return this;
    }

    ;

    /**
     * FROM 절 지정
     *
     * @param table 테이블명
     * @return this
     */
    public SelectQueryBuilder from(String table) {
        froms.add(table);
        return this;
    }

    ;

    /**
     * ORDER BY 절 지정
     *
     * @param column    정렬 컬럼
     * @param direction "ASC" 또는 "DESC"
     * @return this
     */
    public SelectQueryBuilder orderBy(String column, String direction) {
        orders.add(column + " " + direction.toUpperCase());
        return this;
    }

    ;

    /**
     * LIMIT 절 지정
     *
     * @param limit 조회 개수
     * @return this
     */
    public SelectQueryBuilder limit(int limit) {
        limits.add(String.valueOf(limit));
        return this;
    }

    ;

    /**
     * SQL 문자열 생성
     *
     * @return 생성된 SQL
     */
    public String build() {
        if (froms.isEmpty()) {
            throw new IllegalStateException("FROM 절은 필수입니다.");
        }

        if (limits.stream().map(Integer::parseInt).anyMatch(c -> c < 0)) {
            throw new IllegalStateException("LIMIT 값은 음수일 수 없습니다.");
        }

        if (columns.isEmpty()) {
            columns.add("*");
        }

        return "SELECT " + String.join(", ", columns) +
                " FROM " + String.join(", ", froms) +
                (!orders.isEmpty() ? " ORDER BY " + String.join(", ", orders) : "") +
                (!limits.isEmpty() ? " LIMIT " + String.join(", ", limits) : "");

    }
}
