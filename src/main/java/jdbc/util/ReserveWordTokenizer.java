package jdbc.util;

import jdbc.vo.ReserveWord;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReserveWordTokenizer {

    private final List<ReserveWord> reserveWords;
    private final String targetSql;
    private final String upperSql;

    public ReserveWordTokenizer(List<ReserveWord> reserveWords, String targetSql) {
        this.reserveWords = new ArrayList<>(reserveWords);
        this.targetSql = targetSql.replaceAll("\\s*,\\s*", ",").replaceAll("\\s+", " ");
        this.upperSql = this.targetSql.toUpperCase();
        this.reserveWords.sort(Comparator.comparing(ReserveWord::getPriority));
    }

    public Map<String, String> tokenize() {
        this.reserveWords.forEach(this::validate);
        List<Pair> reserveIdxPairs = new ArrayList<>();

        for (ReserveWord reserveWord : this.reserveWords) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(reserveWord.getValue()) + "\\b");
            Matcher matcher = pattern.matcher(this.upperSql);

            if (matcher.find()) {
                int startIdx = matcher.start();
                int endIdx = startIdx + reserveWord.getValue().length() - 1;
                reserveIdxPairs.add(new Pair(startIdx, endIdx, reserveWord.getValue()));
            }
        }

        CharSequence charSequence = this.targetSql;
        Map<String, String> result = new HashMap<>();
        int totalSize = reserveIdxPairs.size();

        for (int i = 0; i < totalSize - 1; i++) {
            int startIdx = reserveIdxPairs.get(i).second() + 1;
            int endIdx = reserveIdxPairs.get(i + 1).first() - 1;
            String betweenReserveWord = ((String) charSequence.subSequence(startIdx, endIdx)).trim();
            if (betweenReserveWord.isBlank()) {
                throw new IllegalArgumentException("예약어 사이에 빈값이 있을 수 없음");
            }
            result.put(reserveIdxPairs.get(i).reserveWordValue, betweenReserveWord);
        }
        int lastStartIdx = reserveIdxPairs.get(totalSize - 1).second + 1;
        int lastEndIdx = this.targetSql.length();
        String lastWord = ((String) charSequence.subSequence(lastStartIdx, lastEndIdx)).trim();

        if (lastWord.isBlank()) {
            throw new IllegalArgumentException("예약어 사이에 빈값이 있을 수 없음");
        }
        result.put(reserveIdxPairs.get(totalSize - 1).reserveWordValue, lastWord);
        return result;
    }

    private void validate(ReserveWord reserveWord) {
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(reserveWord.getValue()) + "\\b");
        Matcher matcher = pattern.matcher(this.upperSql);
        boolean found = matcher.find();

        if (!reserveWord.isRequired() && !found) {
            return;
        }
        if (reserveWord.isRequired() && !found) {
            throw new IllegalArgumentException("필수 예약어가 없음 = " + reserveWord);
        }

        int startIdx = matcher.start();

        if (matcher.find()) {
            throw new IllegalArgumentException("예약어가 2개 이상임 = " + reserveWord);
        }
        if (startIdx + reserveWord.getValue().length() == this.upperSql.length()) {
            throw new IllegalArgumentException("예약어로 끝날 수 없음 = " + reserveWord);
        }
    }

    record Pair(int first, int second, String reserveWordValue) {}
}
