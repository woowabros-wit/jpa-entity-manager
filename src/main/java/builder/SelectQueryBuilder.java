package builder;

public class SelectQueryBuilder implements Query {

    private String[] columns;
    private String table;
    private String orderColumn;
    private Direction orderDirection;
    private Integer limit;

    public SelectQueryBuilder select(String... columns) {
        this.columns = columns;
        return this;
    }

    public SelectQueryBuilder from(String table) {
        this.table = table;
        return this;
    }

    public SelectQueryBuilder orderBy(String column, Direction direction) {
        if (column != null && !column.isBlank()) {
            this.orderColumn = column;
        }
        this.orderDirection = direction;
        return this;
    }

    public SelectQueryBuilder limit(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit은 양수여야 합니다.");
        }
        this.limit = limit;
        return this;
    }

    @Override
    public String build() {
        validateTable();

        return "SELECT " + generateColumnsString()
            + " FROM " + table
            + generateOrderByString()
            + generateLimitString();
    }

    private void validateTable() {
        if (table == null || table.isBlank()) {
            throw new IllegalStateException("table은 null 혹은 공백일 수 없습니다.");
        }
    }

    private String generateColumnsString() {
        return columns != null && columns.length > 0 ? String.join(", ", columns) : "*";
    }

    private String generateOrderByString() {
        if (orderColumn == null) {
            return "";
        }
        StringBuilder orderBy = new StringBuilder(" ORDER BY ").append(orderColumn);
        if (orderDirection != null) {
            orderBy.append(" ").append(orderDirection);
        }
        return orderBy.toString();
    }

    private String generateLimitString() {
        return limit != null ? " LIMIT " + limit : "";
    }
}
