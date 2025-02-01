package calculator;

import java.math.BigInteger;
import java.sql.SQLOutput;
import java.text.DecimalFormat;
import java.util.*;

public class Calculator{
    List<CalcItem> calcItemList;
    FactoryCalculator calcFactory;
    Stack<CalcItem> calcStack;
    Deque<CalcItem> calcDeque;
    Map<String, BigInteger> variables;
    static Calculator instance;

    public Calculator(){
        calcItemList = new ArrayList<>();
        calcFactory = new FactoryCalculator();
        calcStack = new Stack<>();
        calcDeque = new ArrayDeque<>();
        variables = new HashMap<>();
        Calculator.instance = this;
    }

    public void setVariable(String variable, BigInteger value) {
        if(value == null) throw new ArithmeticException("Invalid Expression");
        variables.put(variable, value);
    }

    public BigInteger getVariable(String variable) {
        BigInteger var = variables.get(variable);
        if(var == null) throw new ArithmeticException("Invalid Expression");
        return var;
    }

    private String setupAssignement(String expression){
        long count = expression.chars().filter(ch -> ch == '=').count();
        if(count == 1){
            expression = expression.replaceAll("=", " = ");
        }
        expression = expression.replaceAll("[(]", " ( ");
        expression = expression.replaceAll("[)]", " ) ");

        return expression;
    }

    public void calculate(String expression){
        // delete the list
        try {
            calcItemList.clear();
            calcStack.clear();
            calcDeque.clear();

            expression = setupAssignement(expression);
            String[] tokens = expression.split(" ");
            for (String token : tokens) {
                CalcItem calcItem = calcFactory.createCalcItem(token);
                if (calcItem != null) {
                    calcItemList.add(calcItem);
                }
            }
            createIpongList();
            Operand value = evaluateExpression();
            if (value != null) {
                System.out.println(value.getValue());
            }
        }catch(Exception e) {
            System.out.println(e.getMessage());
            // System.out.println("Invalid expression");
        }

    }

    private void createIpongList(){
        for (CalcItem item : calcItemList) {
            if (item.getType() == TypeExpr.OPERAND || item.getType() == TypeExpr.VARIABLE) {
                calcDeque.addLast(item);
            } else if (item.getType() == TypeExpr.LEFT_PARENTHESIS) {
                calcStack.push(item);
            } else if (item.getType() == TypeExpr.RIGHT_PARENTHESIS) {
                CalcItem i;
                do{
                    i = calcStack.pop();
                    if(i.getType() != TypeExpr.LEFT_PARENTHESIS){
                        calcDeque.addLast(i);
                        if(calcStack.isEmpty()) throw new IllegalArgumentException("Invalid Expression");
                    }

                }while (i.getType() != TypeExpr.LEFT_PARENTHESIS);
            } else if (item.getType() == TypeExpr.OPERATOR || item.getType() == TypeExpr.ASSIGNMENT ) {
                while (!(calcStack.isEmpty() || ((CalcItem)calcStack.peek()).getType() == TypeExpr.LEFT_PARENTHESIS)
                        && ((CalcItem)calcStack.peek()).getPrecedence() >= ((CalcItem)item).getPrecedence()) {
                    calcDeque.addLast(calcStack.pop());
                }
                calcStack.push(item);
            }
        }
        while (!calcStack.isEmpty() && (calcStack.peek().getType() == TypeExpr.OPERATOR ||
                calcStack.peek().getType() == TypeExpr.ASSIGNMENT)) {
            calcDeque.addLast(calcStack.pop());
        }
    }

    public Operand evaluateExpression() {
        while (!calcDeque.isEmpty()) {
            CalcItem item = calcDeque.removeFirst();
            switch (item.getType()) {
                case OPERAND, VARIABLE:
                    calcStack.push(item);
                    break;
                case OPERATOR:
                case ASSIGNMENT:
                    Operator operator = (Operator) item;
                    operator.getValue();
                    break;
            }
        }

        if(calcStack.isEmpty()){
            return null;
        }
        Operand finalResult = (Operand) calcStack.pop();
        if(!calcStack.isEmpty()){
            throw new ArithmeticException("Invalid Expression");
        }
        return finalResult;
    }
}
