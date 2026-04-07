package jdbc.core;

import jdbc.enums.OrderOperand;
import jdbc.util.ReserveWordTokenizer;
import jdbc.util.WhereClauseParser;
import jdbc.vo.*;

import java.util.*;
import java.util.stream.Collectors;

public class SelectQueryBuilder implements JdbcSqlGenerator, NamedParameterTargetProcessor {

    private static final List<ReserveWord> RESERVE_WORDS = List.of(
            new ReserveWord("select", true, 1),
            new ReserveWord("from", true, 2),
            new ReserveWord("where", false, 3),
            new ReserveWord("order by", false, 4),
            new ReserveWord("limit", false, 5)
    );

    SelectTarget selectTarget = new SelectTarget();
    String tableName = null;
    OrderTarget orderTarget;
    Long limitValue = null;
    List<CompareTarget> compareTargetList = new ArrayList<>();

    /**
     * SELECT 절 지정
     * @param columns 컬럼명 (가변 인자)
     * @return this (메서드 체이닝)
     */
    public SelectQueryBuilder select(String... columns) {
        this.selectTarget.addSelectColumns(columns);
        return this;
    }

    /**
     * FROM 절 지정
     * @param table 테이블명
     * @return this
     */
    public SelectQueryBuilder from(String table) {
        if (table == null) {
            throw new IllegalStateException("from에 테이블은 필수값임");
        }
        this.tableName = table;
        return this;
    }

    /**
     * ORDER BY 절 지정
     * @param column 정렬 컬럼
     * @param direction "ASC" 또는 "DESC"
     * @return this
     */
    public SelectQueryBuilder orderBy(String column, String direction) {
        this.orderTarget = OrderOperand.parse(column, direction);
        return this;
    }

    /**
     * LIMIT 절 지정
     * @param limit 조회 개수
     * @return this
     */
    public SelectQueryBuilder limit(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit는 양의 정수여야 함");
        }
        this.limitValue = (long) limit;
        return this;
    }

    /**
     * SQL 문자열 생성
     * @return 생성된 SQL
     */
    public String build() {
        if (tableName == null) {
            throw new IllegalStateException("table명은 필수임");
        }

        String selectString = this.selectTarget.getSelectColumnsString();

        String baseString = "SELECT " + selectString + " FROM " + tableName;

        if (!compareTargetList.isEmpty()) {
            String whereJoinString = this.compareTargetList.getFirst().getJoinString();

            String whereString = this.compareTargetList.stream()
                    .map(CompareTarget::getTargetString)
                    .collect(Collectors.joining(whereJoinString));

            baseString += " WHERE " + whereString;
        }

        if (orderTarget != null) {
            baseString += " " + this.orderTarget.getOrderString();
        }
        if (limitValue != null) {
            baseString += " LIMIT " + this.limitValue + " ";
        }
        return baseString.trim();
    }

    /**
     * WHERE 절 추가
     */
    public SelectQueryBuilder where(String condition) {
        this.compareTargetList = WhereClauseParser.parse(condition);
        return this;
    }

    @Override
    public boolean isSupported(String sql) {
        return sql.startsWith("SELECT") || sql.startsWith("select");
    }

    @Override
    public String makeJdbcQuery(String sql) {
        ReserveWordTokenizer reserveWordTokenizer = new ReserveWordTokenizer(RESERVE_WORDS, sql);
        Map<String, String> nonReserved = reserveWordTokenizer.tokenize();
        String[] selectClause = nonReserved.get("SELECT").split(",");
        String fromTable = nonReserved.get("FROM");

        String whereClause = nonReserved.get("WHERE");
        String orderByClause = nonReserved.get("ORDER BY");
        String limitCount = nonReserved.get("LIMIT");

        SelectQueryBuilder builder = this.select(selectClause)
                .from(fromTable);

        if (whereClause != null) {
            builder.where(whereClause);
        }
        if (orderByClause != null) {
            String[] parsedOrderByClause = orderByClause.split(" ");
            int orderByClauseSize = parsedOrderByClause.length;
            builder.orderBy(parsedOrderByClause[0], (orderByClauseSize == 2) ? parsedOrderByClause[1] : null);
        }
        if (limitCount != null) {
            builder.limit(Integer.parseInt(limitCount));
        }
        return builder.build();
    }

    private void processOrders() {
        int order = 1;
        for (CompareTarget compareTarget : this.compareTargetList) {
            if (!compareTarget.isNamedParameterEmpty()) {
                compareTarget.setOrder(order);
                order += 1;
            }
        }
    }

    @Override
    public void processParameter(String name, Object value) {
        Set<String> wholeNames = new HashSet<>();

        wholeNames.addAll(this.compareTargetList.stream().map(NamedParameterTarget::getNamedParameter).collect(Collectors.toSet()));

        if (wholeNames.isEmpty()) {
            throw new IllegalArgumentException("namedParameter가 존재하지 않아 바인딩 불가");
        }

        if (!wholeNames.contains(name)) {
            throw new IllegalArgumentException("namedParameter에 대응되는 이름이 없음 = " + name);
        }

        this.compareTargetList.forEach(it -> it.setValue(name, value));
    }

    @Override
    public List<NamedParameterBindCommand> getCommand() {
        processOrders();
        return this.compareTargetList.stream()
                .filter(it -> !it.isNamedParameterEmpty())
                .map(it -> new NamedParameterBindCommand(it.getOrder(), it.getValue()))
                .collect(Collectors.toList());
    }
}
