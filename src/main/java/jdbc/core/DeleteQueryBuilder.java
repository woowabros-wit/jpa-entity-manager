package jdbc.core;

import jdbc.util.ReserveWordTokenizer;
import jdbc.util.WhereClauseParser;
import jdbc.vo.CompareTarget;
import jdbc.vo.NamedParameterBindCommand;
import jdbc.vo.NamedParameterTarget;
import jdbc.vo.ReserveWord;

import java.util.*;
import java.util.stream.Collectors;

public class DeleteQueryBuilder implements JdbcSqlGenerator, NamedParameterTargetProcessor {

    private static final List<ReserveWord> RESERVE_WORDS = List.of(
            new ReserveWord("from", true, 1),
            new ReserveWord("where", true, 2)
    );

    private String tableName;
    private List<CompareTarget> compareTargetList = new ArrayList<>();

    /**
     * DELETE할 테이블 지정
     */
    public DeleteQueryBuilder from(String table) {
        if (table == null) {
            throw new IllegalArgumentException("update 대상 테이블이 없음");
        }
        this.tableName = table;
        return this;
    }

    /**
     * WHERE 절 추가
     */
    public DeleteQueryBuilder where(String condition) {
        this.compareTargetList = WhereClauseParser.parse(condition);
        return this;
    }

    /**
     * SQL 생성
     */
    public String build() {
        if (compareTargetList.isEmpty()) {
            throw new IllegalStateException("where절은 필수임");
        }
        if (tableName == null) {
            throw new IllegalStateException("table명은 필수임");
        }

        String whereJoinString = this.compareTargetList.getFirst().getJoinString();

        String whereString = this.compareTargetList.stream()
                .map(CompareTarget::getTargetString)
                .collect(Collectors.joining(whereJoinString));

        return "DELETE FROM " + tableName + " WHERE " + whereString;
    }

    @Override
    public boolean isSupported(String sql) {
        return sql.startsWith("DELETE") || sql.startsWith("delete");
    }

    @Override
    public String makeJdbcQuery(String sql) {
        ReserveWordTokenizer reserveWordTokenizer = new ReserveWordTokenizer(RESERVE_WORDS, sql);
        Map<String, String> nonReserved = reserveWordTokenizer.tokenize();
        String table = nonReserved.get("FROM");
        String whereClause = nonReserved.get("WHERE");

        return this.from(table)
                .where(whereClause)
                .build();
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
