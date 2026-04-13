package persistence.query;

public class DeleteQueryBuilder {
    private String tableName;
    private String whereCondition;

    public DeleteQueryBuilder from(String table) {
        this.tableName = table;
        return this;
    }

    public DeleteQueryBuilder where(String condition) {
        this.whereCondition = condition;
        return this;
    }

    public String build() {
        if (tableName == null) {
            throw new IllegalStateException();
        }

        if (whereCondition == null || whereCondition.isEmpty()) {
            throw new IllegalStateException();
        }

        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(tableName);
        sql.append(" WHERE ").append(whereCondition);

        return sql.toString();
    }
}