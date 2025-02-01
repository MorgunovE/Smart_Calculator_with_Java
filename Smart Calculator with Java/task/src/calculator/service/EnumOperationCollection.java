package calculator.service;

import calculator.util.CustomException;

import java.math.BigInteger;
import java.util.Arrays;

public enum EnumOperationCollection {

    ADD("+") {
        @Override
        public BigInteger execute(BigInteger a, BigInteger b) {
            return a.add(b);
        }
    },
    SUBTRACT("-") {
        @Override
        public BigInteger execute(BigInteger a, BigInteger b) {
            return a.subtract(b);
        }
    },
    MULTIPLY("*") {
        @Override
        public BigInteger execute(BigInteger a, BigInteger b) {
            return a.multiply(b);
        }
    },
    DIVIDE("/") {
        @Override
        public BigInteger execute(BigInteger a, BigInteger b) {
            return a.divide(b);
        }
    };

    private final String representation;

    EnumOperationCollection(String representation) {
        this.representation = representation;
    }

    public static EnumOperationCollection getByRepresentation(String representation) throws CustomException {
        return Arrays.stream(values()).filter(o -> o.representation.equals(representation))
                .findAny().orElseThrow(() ->
                        new CustomException("No operand with representation : " + representation));
    }

    public abstract BigInteger execute(BigInteger a, BigInteger b);
}