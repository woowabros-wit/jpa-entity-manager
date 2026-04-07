package jdbc.util;

import jdbc.enums.CompareOperand;
import jdbc.enums.LogicOperand;
import jdbc.vo.CompareTarget;

import java.util.ArrayList;
import java.util.List;

public class WhereClauseParser {

    public static List<CompareTarget> parse(String whereClause) {
        if (whereClause.startsWith("where")) {
            throw new IllegalArgumentException("where로 시작하지 않아야 함 : " + whereClause);
        }

        LogicOperand logicOperand = LogicOperand.from(whereClause);
        List<String> parsedByLogicOperand = logicOperand.parse(whereClause);

        List<CompareTarget> result = new ArrayList<>();

        parsedByLogicOperand.forEach(
                it -> {
                    CompareOperand compareOperand = CompareOperand.from(it);
                    result.add(compareOperand.parse(it, logicOperand));
                }
        );
        return result;
    }
}
