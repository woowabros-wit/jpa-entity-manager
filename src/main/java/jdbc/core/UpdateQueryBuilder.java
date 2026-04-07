package jdbc.core;

import jdbc.util.ReserveWordTokenizer;
import jdbc.util.WhereClauseParser;
import jdbc.vo.*;

import java.util.*;
import java.util.stream.Collectors;

public class UpdateQueryBuilder implements JdbcSqlGenerator, NamedParameterTargetProcessor {

    private static final List<ReserveWord> RESERVE_WORDS = List.of(
            new ReserveWord("update", true, 1),
            new ReserveWord("set", true, 2),
            new ReserveWord("where", true, 3)
    );

    private Map<String, String> updateValues = new LinkedHashMap<>();
    private String tableName;
    private List<SettingTarget> settingTargetList = new LinkedList<>();
    private List<CompareTarget> compareTargetList = new ArrayList<>();


    /**
     * UPDATE할 테이블 지정
     */
    public UpdateQueryBuilder table(String table) {
        if (table == null) {
            throw new IllegalArgumentException("update 대상 테이블이 없음");
        }
        this.tableName = table;
        return this;
    }


    /**
     * SET 절에 컬럼-값 추가
     */
    public UpdateQueryBuilder set(String column, String value) {
        if (column == null) {
            throw new IllegalArgumentException("insert 대상 칼럼이 없음");
        }
        this.updateValues.put(column, value);
        this.settingTargetList.add(new SettingTarget(column, value));
        return this;
    }

    /**
     * WHERE 절 추가
     */
    public UpdateQueryBuilder where(String condition) {
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

        String setString = this.settingTargetList.stream()
                .map(SettingTarget::getTargetString)
                .collect(Collectors.joining(", "));

        String whereJoinString = this.compareTargetList.getFirst().getJoinString();

        String whereString = this.compareTargetList.stream()
                .map(CompareTarget::getTargetString)
                .collect(Collectors.joining(whereJoinString));

        return "UPDATE " + this.tableName + " SET " + setString + " WHERE " + whereString;
    }

    @Override
    public boolean isSupported(String sql) {
        return sql.startsWith("UPDATE") || sql.startsWith("update");
    }

    @Override
    public String makeJdbcQuery(String sql) {
        ReserveWordTokenizer reserveWordTokenizer = new ReserveWordTokenizer(RESERVE_WORDS, sql);
        Map<String, String> nonReserved = reserveWordTokenizer.tokenize();
        String table = nonReserved.get("UPDATE");
        String[] setClause = nonReserved.get("SET").split(",");
        String whereClause = nonReserved.get("WHERE");

        UpdateQueryBuilder builder = this.table(table);

        Arrays.stream(setClause).forEach(it -> {
            String[] parsedSetClause = it.split(" = ");
            if (parsedSetClause.length != 2) {
                throw new IllegalArgumentException("SET 시 (key = value) 구조가 아닙니다");
            }

            builder.set(parsedSetClause[0], parsedSetClause[1]);
        });

        return builder.where(whereClause)
                .build();
    }

    private void processOrders() {
        int order = 1;
        for (SettingTarget settingTarget : this.settingTargetList) {
            if (!settingTarget.isNamedParameterEmpty()) {
                settingTarget.setOrder(order);
                order += 1;
            }
        }
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
        wholeNames.addAll(this.settingTargetList.stream().map(NamedParameterTarget::getNamedParameter).collect(Collectors.toSet()));

        if (wholeNames.isEmpty()) {
            throw new IllegalArgumentException("namedParameter가 존재하지 않아 바인딩 불가");
        }

        if (!wholeNames.contains(name)) {
            throw new IllegalArgumentException("namedParameter에 대응되는 이름이 없음 = " + name);
        }

        this.compareTargetList.forEach(it -> it.setValue(name, value));
        this.settingTargetList.forEach(it -> it.setValue(name, value));
    }

    @Override
    public List<NamedParameterBindCommand> getCommand() {
        processOrders();
        List<NamedParameterBindCommand> commandsFromCompare = this.compareTargetList.stream()
                .filter(it -> !it.isNamedParameterEmpty())
                .map(it -> new NamedParameterBindCommand(it.getOrder(), it.getValue()))
                .collect(Collectors.toList());

        List<NamedParameterBindCommand> commandsFromSet = this.settingTargetList.stream()
                .filter(it -> !it.isNamedParameterEmpty())
                .map(it -> new NamedParameterBindCommand(it.getOrder(), it.getValue()))
                .collect(Collectors.toList());

        commandsFromSet.addAll(commandsFromCompare);
        return commandsFromSet;
    }
}
