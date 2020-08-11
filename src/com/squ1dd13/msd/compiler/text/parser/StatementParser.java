package com.squ1dd13.msd.compiler.text.parser;

import com.squ1dd13.msd.compiler.text.lexer.*;

import java.util.*;

// Parser that can read code with statements separated by semicolons.
public class StatementParser extends GenericParser {
    public StatementParser(Iterator<Token> tokenIterator) {
        super(tokenIterator);
    }

    public StatementParser(List<Token> tokenList) {
        super(tokenList);
    }

    private List<Token> nextStatement() {
        List<Token> statement = new ArrayList<>();

        while(tokenIterator.hasNext()) {
            Token token = tokenIterator.next();

            if(token.is(Token.TokenType.Semicolon)) break;
            statement.add(token);
        }

        return statement;
    }
}
