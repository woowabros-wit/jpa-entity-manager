package persistence;

public class DeleteQueryBuilder {

    private String table;
    private String condition;

    public DeleteQueryBuilder from(String table) {
        this.table = table;
        return this;
    }

    public DeleteQueryBuilder where(String condition) {
        this.condition = condition;
        return this;
    }

    public String build() {
        if (table == null || table.isBlank()) {
            throw new IllegalStateException("테이블 지정(from)은 필수입니다.");
        }
        if (condition == null || condition.isBlank()) {
            throw new IllegalStateException("WHERE 조건은 필수입니다.");
        }

        return "DELETE FROM " + table + " WHERE " + condition;
    }
}
