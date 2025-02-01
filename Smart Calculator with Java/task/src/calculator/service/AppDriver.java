package calculator.service;

import calculator.controller.inputController;
import calculator.util.CustomException;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class AppDriver {
    private final inputController userInputProcessor = new inputController();
    private final Map<String, BigInteger> variableMap = new HashMap<>();

    public void loopInput() {
        boolean run = true;
        do {
            String input = userInputProcessor.inputLine();
            if ("".equals(input)) {
                continue;
            }
            try {
                run = processLine(input);
            } catch (CustomException e) {
                System.out.println(e.getMessage());
            }
        } while (run);
    }

    private boolean optionProcessor(String input) throws CustomException {
        EnumOptionsCollection option;
        try {
            option = EnumOptionsCollection.valueOf(input.toUpperCase().substring(1));
        } catch (IllegalArgumentException e) {
            throw new CustomException("Unknown command");
        }
        System.out.println(option);
        return option.isRun();
    }

    private boolean processLine(String line) throws CustomException {
        line = line.trim();
        if (line.contains("=")) {
            processAssignments(line);
            return true;
        }
        List<String> strings = Arrays.stream(line.split("\\s+"))
                .collect(Collectors.toList());
        if (strings.size() == 1) {
            String s = strings.get(0);
            if (isParsable(s)) {
                System.out.println(Integer.parseInt(s));
                return true;
            } else {
                if (Objects.equals('/', s.charAt(0))) {
                    return optionProcessor(s);
                }
                if (isVariable(s)) {
                    System.out.println(variableMap.get(s));
                    return true;
                }
                throw new CustomException("Unknown variable : " + line);
            }
        }
        OperationService solver = new OperationService(line, variableMap);
        solver.execute();
        return true;
    }

    private void processAssignments(String line) {
        List<String> strings = Arrays.stream(line.split("=")).map(String::trim)
                .collect(Collectors.toList());
        String variable = strings.get(1);
        if (strings.size() > 2 || (!isParsable(variable) && !isVariable(variable))) {
            throw new CustomException("Invalid assignment for : " + line);
        }
        if (!strings.get(0).matches("[A-Za-z]+")) {
            throw new CustomException("Invalid identifier");
        }
        if (!isVariable(variable) && !isParsable(variable)) {
            throw new CustomException("Unknown variable : " + variable);
        }
        BigInteger a;
        if (isVariable(variable)) {
            a = variableMap.get(variable);
        } else {
            a = new BigInteger(variable);
        }
        variableMap.put(strings.get(0), a);
    }

    private boolean isParsable(String str) {
        try {
            new BigInteger(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isVariable(String s) {
        return variableMap.containsKey(s);
    }
}