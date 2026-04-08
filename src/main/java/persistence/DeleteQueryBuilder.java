package persistence;

public class DeleteQueryBuilder {

    private String table;
    private String whereCondition;

    /**
     * DELETE할 테이블 지정
     */
    public DeleteQueryBuilder from(String table) {
        this.table = table;
        return this;
    }

    /**
     * WHERE 절 추가
     */
    public DeleteQueryBuilder where(String condition) {
        this.whereCondition = condition;
        return this;
    }

    /**
     * SQL 생성
     */
    public String build() {
        if (table == null || table.isEmpty()) {
            throw new IllegalStateException("테이블 이름이 지정되지 않았습니다.");
        }
        if (whereCondition == null || whereCondition.isEmpty()) {
            throw new IllegalStateException("WHERE 조건이 지정되지 않았습니다. DELETE 쿼리는 WHERE 절이 필수입니다.");
        }
        return "DELETE FROM " + table + " WHERE " + whereCondition;
    }
}

