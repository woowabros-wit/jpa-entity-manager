package jdbc.vo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SelectTarget {

    private final List<String> names = new ArrayList<>();

    public SelectTarget() {
        names.add("*");
    }

    public void addSelectColumns(String... columns) {
        List<String> inputColumns = Arrays.stream(columns).toList();
        int uniqueColumnCnt = Set.of(columns).size();

        if (uniqueColumnCnt != inputColumns.size()) {
            throw new IllegalArgumentException("select에 중복된 칼럼이 있음");
        } else {
            this.names.clear();
            this.names.addAll(inputColumns);
        }
    }

    public String getSelectColumnsString() {
        return String.join(", ", this.names);
    }
}
