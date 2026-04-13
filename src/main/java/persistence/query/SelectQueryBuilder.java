package persistence.query;

import java.util.Arrays;
import java.util.List;

public class SelectQueryBuilder {
    private List<String> columns;
    private String tableName;
    private String whereCondition;
    private String orderByColumn;
    private DirectionType direction;
    private Integer limit;

    public SelectQueryBuilder select(String... columns) {
        this.columns = Arrays.asList(columns);
        return this;
    }

    public SelectQueryBuilder from(String table) {
        this.tableName = table;
        return this;
    }

    public SelectQueryBuilder where(String condition) {
        this.whereCondition = condition;
        return this;
    }

    public SelectQueryBuilder orderBy(String column, String direction) {
        this.orderByColumn = column;
        this.direction = DirectionType.from(direction);
        return this;
    }

    public SelectQueryBuilder limit(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException();
        }
        this.limit = limit;
        return this;
    }

    public String build() {
        if (tableName == null) {
            throw new IllegalStateException();
        }

        StringBuilder sql = new StringBuilder();

        if (columns == null || columns.isEmpty()) {
            sql.append("SELECT *");
        } else {
            sql.append("SELECT ");
            sql.append(String.join(", ", columns));
        }

        sql.append(" FROM ").append(tableName);

        if (whereCondition != null) {
            sql.append(" WHERE ").append(whereCondition);
        }

        if (orderByColumn != null) {
            sql.append(" ORDER BY ").append(orderByColumn);
            if (direction != null) {
                sql.append(" ").append(direction);
            }
        }

        if (limit != null) {
            sql.append(" LIMIT ").append(limit);
        }

        return sql.toString();
    }
}