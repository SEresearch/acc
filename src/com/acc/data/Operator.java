package com.acc.data;

import com.acc.exception.UnknownOperatorException;

/**
 * Created by Rumpy on 15-01-2015.
 */
public class Operator extends Token {
    private Operators value;

    public Operator(String token) {
        super(token);
        assignValue();
    }

    private void assignValue() {
        if (getToken().equals("+")) {
            value = Operators.PLUS;
        } else if (getToken().equals("-")) {
            value = Operators.MINUS;
        } else if (getToken().equals("/")) {
            value = Operators.DIVISION;
        } else if (getToken().equals("*")) {
            value = Operators.PRODUCT;
        }
        throw new UnknownOperatorException(getToken());
    }

    @Override
    public TokenType tokenType() {
        return TokenType.OPERATOR;
    }

    public Operators value() {
        return value;
    }

}