package jdbc.core;

import jdbc.util.ReserveWordTokenizer;
import jdbc.vo.NamedParameterBindCommand;
import jdbc.vo.NamedParameterTarget;
import jdbc.vo.ReserveWord;
import jdbc.vo.SettingTarget;

import java.util.*;
import java.util.stream.Collectors;

public class InsertQueryBuilder implements JdbcSqlGenerator, NamedParameterTargetProcessor {

    private static final List<ReserveWord> RESERVE_WORDS = List.of(
            new ReserveWord("insert into", true, 1),
            new ReserveWord("values", true, 2)
    );

    private final Map<String, String> insertValues = new LinkedHashMap<>();
    private final List<SettingTarget> settingTargetList = new LinkedList<>();
    private String tableName;

    /**
     * INSERT할 테이블 지정
     */
    public InsertQueryBuilder into(String table) {
        if (table == null) {
            throw new IllegalArgumentException("insert 대상 테이블이 없음");
        }
        this.tableName = table;
        return this;
    }

    /**
     * 컬럼-값 쌍 추가
     * @param column 컬럼명
     * @param value 값 (?, 파라미터 플레이스홀더)
     */
    public InsertQueryBuilder value(String column, String value) {
        if (column == null) {
            throw new IllegalArgumentException("insert 대상 칼럼이 없음");
        }
        this.insertValues.put(column, value);
        this.settingTargetList.add(new SettingTarget(column, value));
        return this;
    }

    /**
     * 여러 컬럼-값 쌍을 Map으로 추가
     */
    public InsertQueryBuilder values(Map<String, String> columnValues) {
        columnValues.forEach(this::value);
        return this;
    }

    /**
     * SQL 생성
     */
    public String build() {
        if (tableName == null) {
            throw new IllegalStateException("insert 대상 테이블이 없음");
        }

        if (this.settingTargetList.isEmpty()) {
            throw new IllegalStateException("최소 하나 이상의 column을 insert 해야함");
        }
        String columns = this.settingTargetList.stream()
                .map(SettingTarget::getName)
                .collect(Collectors.joining(", "));

        String displayValues = this.settingTargetList.stream()
                .map(SettingTarget::getDisplayValue)
                .collect(Collectors.joining(", "));

        return "INSERT INTO " + this.tableName + " (" + columns + ") VALUES (" + displayValues + ")";
    }

    @Override
    public boolean isSupported(String sql) {
        return sql.startsWith("INSERT") || sql.startsWith("insert");
    }

    @Override
    public String makeJdbcQuery(String sql) {
        ReserveWordTokenizer reserveWordTokenizer = new ReserveWordTokenizer(RESERVE_WORDS, sql);
        Map<String, String> nonReserved = reserveWordTokenizer.tokenize();
        String[] parserNonReserved = nonReserved.get("INSERT INTO").split(" ");
        String table = parserNonReserved[0];
        String[] columns = parserNonReserved[1].split(",");
        String[] values = nonReserved.get("VALUES").split(",");

        if (columns.length != values.length) {
            throw new IllegalArgumentException("insert 대상 칼럼과 값 개수가 일치하지 않음");
        }

        InsertQueryBuilder builder = this.into(table);

        for (int i = 0; i < columns.length; i++) {
            String name = columns[i].replace("(", "").replace(")", "");
            String value = values[i].replace("(", "").replace(")", "");

            builder.value(name, value);
        }

        return builder.build();
    }

    private void processOrders() {
        int order = 1;
        for (SettingTarget settingTarget : this.settingTargetList) {
            if (!settingTarget.isNamedParameterEmpty()) {
                settingTarget.setOrder(order);
                order += 1;
            }
        }
    }

    @Override
    public void processParameter(String name, Object value) {
        Set<String> wholeNames = new HashSet<>();

        wholeNames.addAll(this.settingTargetList.stream().map(NamedParameterTarget::getNamedParameter).collect(Collectors.toSet()));

        if (wholeNames.isEmpty()) {
            throw new IllegalArgumentException("namedParameter가 존재하지 않아 바인딩 불가");
        }

        if (!wholeNames.contains(name)) {
            throw new IllegalArgumentException("namedParameter에 대응되는 이름이 없음 = " + name);
        }

        this.settingTargetList.forEach(it -> it.setValue(name, value));
    }

    @Override
    public List<NamedParameterBindCommand> getCommand() {
        processOrders();
        return this.settingTargetList.stream()
                .filter(it -> !it.isNamedParameterEmpty())
                .map(it -> new NamedParameterBindCommand(it.getOrder(), it.getValue()))
                .collect(Collectors.toList());
    }
}
