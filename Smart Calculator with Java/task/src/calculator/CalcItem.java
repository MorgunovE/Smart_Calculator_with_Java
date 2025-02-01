package calculator;

import java.math.BigInteger;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

enum TypeExpr{
    NONE,
    OPERATOR,
    OPERAND,
    ASSIGNMENT,
    VARIABLE,
    LEFT_PARENTHESIS,
    RIGHT_PARENTHESIS
}

class FactoryCalculator{

    static TypeExpr isTypeOf(String expression){
        if(expression.isEmpty()) return TypeExpr.NONE;

        Pattern r = Pattern.compile("[+-]?\\d+");
        Matcher m = r.matcher(expression);
        if(m.matches()) return TypeExpr.OPERAND;

        r = Pattern.compile("[+-]?[A-Za-z]+");
        m = r.matcher(expression);
        if(m.matches()) return TypeExpr.VARIABLE;

        if(expression.equals("=")){
            return TypeExpr.ASSIGNMENT;
        }

        if(expression.equals("(")){
            return TypeExpr.LEFT_PARENTHESIS;
        }
        if(expression.equals(")")){
            return TypeExpr.RIGHT_PARENTHESIS;
        }

        r = Pattern.compile("^[-+]*$");
        m = r.matcher(expression);
        if(m.matches()) return TypeExpr.OPERATOR;

        r = Pattern.compile("[*/]{1}");
        m = r.matcher(expression);
        if(m.matches()) return TypeExpr.OPERATOR;


        return TypeExpr.NONE;
    }

    public static CalcItem createCalcItem(String expression) {
        if(expression.isEmpty()) return null;

        switch(FactoryCalculator.isTypeOf(expression)){
            case OPERATOR:
                return new Operator(expression);
            case OPERAND:
                return new Operand(expression);
            case ASSIGNMENT:
                return new Assignment(expression);
            case VARIABLE:
                return new Variable(expression);
            case LEFT_PARENTHESIS:
                return new LeftParenthesis();
            case RIGHT_PARENTHESIS:
                return new RightParenthesis();
            default:
                throw new IllegalArgumentException("Invalid expression");
        }
    }
}

abstract class CalcItem{
    TypeExpr type;
    int precedence = 0;

    public CalcItem(){
        setType();
    }

    public int getPrecedence() {return precedence;}

    abstract void setType();
    TypeExpr getType() {return type;}
}

class LeftParenthesis extends CalcItem{
    LeftParenthesis(){
        super();
        precedence = 6;
    }

    @Override
    void setType(){
        type = TypeExpr.LEFT_PARENTHESIS;
    }
}

class RightParenthesis extends CalcItem{
    RightParenthesis(){
        super();
        precedence = 6;
    }

    @Override
    void setType(){
        type = TypeExpr.RIGHT_PARENTHESIS;
    }
}

class Operator extends CalcItem{
    String operator;

    public String getOperator() {
        return operator;
    }

    @Override
    void setType(){
        type = TypeExpr.OPERATOR;
    }

    public Operator()
    {
        super();
    }
    public Operator(String expression) {
        super();
        if(expression.equals("*") || expression.equals("/")) {
            operator = expression;
            precedence = 3;
            return;
        }
        long countPlus = expression.chars().filter(ch -> ch == '+').count();
        long countMinus = expression.chars().filter(ch -> ch == '-').count();
        if(countMinus % 2 == 1){
            operator = "-";
            precedence = 2;
            return;
        }
        if(countPlus > 0 || countMinus % 2 == 0){
            operator = "+";
            precedence = 1;
            return;
        }
        throw new IllegalArgumentException("Invalid expression");
    }

    public void getValue() {
        Operand b = (Operand) Calculator.instance.calcStack.pop();
        Operand a = (Operand) Calculator.instance.calcStack.pop();

        if(!(b.getType() == TypeExpr.OPERAND || b.getType() == TypeExpr.VARIABLE) &&
                (a.getType() == TypeExpr.OPERAND || a.getType() == TypeExpr.VARIABLE)){
            throw new IllegalArgumentException("Invalid expression");
        }
        BigInteger result = BigInteger.ZERO;
        switch(operator){
            case "+":
                result = a.getValue().add(b.getValue());
                break;
            case "-":
                result = a.getValue().subtract(b.getValue());
                break;
            case "*":
                result = a.getValue().multiply(b.getValue());
                break;
            case "/":
                result = a.getValue().divide(b.getValue());
                break;
            default:
                throw new IllegalArgumentException("Invalid expression");
        }
        Operand resultOperand = new Operand(result);
        Calculator.instance.calcStack.push(resultOperand);

    }
}

class Assignment extends Operator{

    public Assignment(String expression) {
        super();
        if(expression.equals("=")){
            operator = "=";
            precedence = 10;
            return;
        }
        throw new IllegalArgumentException("Invalid expression");
    }

    @Override
    void setType() {
        type = TypeExpr.ASSIGNMENT;
    }

    @Override
    public void getValue() {
        try {
            Operand a = (Operand) Calculator.instance.calcStack.pop();
            Variable v = (Variable) Calculator.instance.calcStack.pop();

            if(a.getType() != TypeExpr.OPERAND && v.getType() != TypeExpr.VARIABLE){
                throw new IllegalArgumentException("Invalid expression");
            }

            if(!Calculator.instance.calcStack.isEmpty()){
                throw new IllegalArgumentException("Invalid expression");
            }

            v.setValue(a.getValue());
        }catch(Exception e){
            throw new IllegalArgumentException("Invalid expression");
        }
    }


}

class Operand extends CalcItem{
    protected String name;

    @Override
    void setType(){
        type = TypeExpr.OPERAND;
    }

    public Operand(BigInteger expression) {
        super();
        name = expression.toString();
    }

    public Operand(String expression) {
        super();
        this.name = expression;
    }

    public BigInteger getValue() {
        return new BigInteger(name);
    }
}

class Variable extends Operand{

    @Override
    void setType(){
        type = TypeExpr.VARIABLE;
    }

    public Variable(String name){
        super(name);
    }

    public String getName() {return name;}

    @Override
    public BigInteger getValue() {
        try {
            return Calculator.instance.getVariable(name);
        }catch (Exception e){
            throw new IllegalArgumentException("Unknown variable");
        }
    }

    public void setValue(BigInteger value){
        Calculator.instance.setVariable(name, value);
    }
}