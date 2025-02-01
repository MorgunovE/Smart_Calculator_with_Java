package calculator.service;

import calculator.util.CustomException;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class OperationService {
    private static final String EXPRESSION_SPLIT = "(?<=\\d)(?=\\D)|(?<=\\D)(?=\\d)|(?<=\\D)(?=\\D)";
    private final String line;
    private final List<String> tokens;
    private final Deque<BigInteger> numbers = new ArrayDeque<>();
    private final Deque<String> operators = new ArrayDeque<>();
    private final Map<String, BigInteger> variables;

    public OperationService(String line, Map<String, BigInteger> variables) {
        this.variables = variables;
        this.line = transformLine(line);
        tokens = Arrays.stream(this.line.split(EXPRESSION_SPLIT))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public void execute() throws CustomException {
        if (!isValid()) {
            throw new CustomException("Invalid expression");
        }
        for (String s : tokens) {
            if (isParsable(s) || variables.containsKey(s)) {
                BigInteger a;
                if (variables.containsKey(s)) {
                    a = variables.get(s);
                } else {
                    a = new BigInteger(s);
                }
                numbers.addLast(a);
            } else {
                processOperator(s);
            }
        }
        while (operators.size() > 0) {
            process(operators.pollLast());
        }
        System.out.println(numbers.getLast());
    }

    private void processOperator(String s) throws CustomException {
        if (operators.peek() == null) {
            operators.addLast(s);
            return;
        } else if (s.equals("(")) {
            operators.addLast(s);
            return;
        }
        if (")".equals(s)) {
            String op;
            do {
                op = operators.pollLast();
                if (!"(".equals(op)) {
                    process(op);
                }
            } while (!"(".equals(op));
            return;
        }
        String op = operators.peekLast();
        if (isHigher(s, op) > 0) {
            operators.addLast(s);
            return;
        }
        while (isHigher(s, op) <= 0 && !"(".equals(op) && operators.size() > 0) {
            op = operators.pollLast();
            process(op);
            op = operators.peekLast();
        }
        operators.addLast(s);
    }


    private void process(String op) throws CustomException {
        BigInteger a;
        BigInteger b;
        try {
            a = numbers.pollLast();
            b = numbers.pollLast();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("One or both numbers could not be extracted " + op);
        }
        EnumOperationCollection operator = EnumOperationCollection.getByRepresentation(op);
        BigInteger result = operator.execute(b, a);
        numbers.addLast(result);
    }

    private int isHigher(String str1, String str2) {
        int o1 = "+".equals(str1) || "-".equals(str1) ? 1 : 2;
        int o2 = "+".equals(str2) || "-".equals(str2) ? 1 : 2;
        return o1 - o2;
    }

    private boolean isValid() {
        AtomicInteger p = new AtomicInteger();
        Matcher mulMatcher = Pattern.compile("\\*{2,}").matcher(line);
        if (mulMatcher.find()) {
            return false;
        }
        Matcher divMatcher = Pattern.compile("/{2,}").matcher(line);
        if (divMatcher.find()) {
            return false;
        }
        for (String s : tokens) {
            if (line.matches("\\*{2,}") || line.matches("/{2,}")) {
                return false;
            }
            if (s.split("\\s+").length > 1) {
                return false;
            }
            p.addAndGet("(".equals(s) ? 1 : 0);
            p.addAndGet(-(")".equals(s) ? 1 : 0));
        }
        return p.get() == 0;
    }

    private boolean isParsable(String str) {
        try {
            new BigInteger(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String transformLine(String line) {
        line = line.replaceAll("\\s+", "");
        for (Map.Entry<String, BigInteger> entry : variables.entrySet()) {
            if (line.contains(entry.getKey())) {
                line = line.replaceAll(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        line = line.replaceAll("-\\+|\\+-", "-");
        line = line.replaceAll("\\++", "+");
        Matcher matcher = Pattern.compile("-+").matcher(line);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String b = matcher.group();
            b = b.length() % 2 == 0 ? "+" : "-";
            matcher.appendReplacement(sb, b);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}