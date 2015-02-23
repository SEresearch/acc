package com.acc.parser;

import com.acc.data.*;
import com.acc.exception.SyntaxErrorException;
import com.acc.util.Tokenizer;

/**
 * Created by prabhuk on 2/12/2015.
 */
public class FunctionDeclaration extends Parser {

    public FunctionDeclaration(Code code, Tokenizer tokenizer, SSACode ssaCode) {
        super(code, tokenizer, ssaCode);
    }

    @Override
    public Result parse() {
        Token next = tokenizer.next();
        if (!isFunctionOrProcedureKeyword(next)) {
            // No function declaration in the file
            tokenizer.previous();
            return null;
        }
        next = tokenizer.next();
        if (!next.isIdentifier()) {
            throw new SyntaxErrorException(next.tokenType(), TokenType.IDENTIFIER);
        }
        //$TODO$ DEAL WITH ident

        if (!isSemiColonToken(tokenizer.next())) {
            tokenizer.previous();
            new FormalParam(code, tokenizer, ssaCode).parse();
            next = tokenizer.next();
            if (!isSemiColonToken(next)) {
                throw new SyntaxErrorException("Expected \";\" keyword. Found [" + next.getToken() + "] instead");
            }
        }


        new FunctionBody(code, tokenizer, ssaCode).parse();
        next = tokenizer.next();
        if (!isSemiColonToken(next)) {
            throw new SyntaxErrorException("Expected \";\" keyword. Found [" + next.getToken() + "] instead");
        }
        return null;
    }

    private boolean isSemiColonToken(Token next) {
        return next.getToken().equals(";");
    }

    private boolean isFunctionOrProcedureKeyword(Token next) {
        return next.isKeyword() && (((Keyword) next).isFunction() || ((Keyword) next).isProcedure());
    }
}
