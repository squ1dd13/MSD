package com.squ1dd13.msd.old.compiler.text.parser;

import com.squ1dd13.msd.old.compiler.text.lexer.*;
import com.squ1dd13.msd.old.shared.*;

import java.util.*;
import java.util.function.*;

// Normal iterator but with peeking and some other stuff.
public class TokenIterator implements Iterator<Token> {
    private final List<Token> tokenList;
    private int tokenIndex = 0;

    public TokenIterator(List<Token> tokenList) {
        this.tokenList = tokenList;
    }

    public TokenIterator(List<Token> tokenList, int tokenIndex) {
        this.tokenList = tokenList;
        this.tokenIndex = tokenIndex;
    }

    @Override
    public boolean hasNext() {
        return tokenIndex < tokenList.size() - 1;
    }

    @Override
    public Token next() {
        return tokenList.get(tokenIndex++);
    }

    public Token peek() {
        return tokenList.get(tokenIndex);
    }

    public Optional<Token> nextIfMatches(Token matchToken, String errorMessage) {
        var token = peek();

        if(ParserUtils.tokenMatches(matchToken, token)) {
            return Optional.ofNullable(next());
        }

        if(errorMessage != null) {
            Util.emitFatalError(errorMessage);
        }

        return Optional.empty();
    }

    public void advance() {
        tokenIndex++;
    }

    public Optional<Token> nextIfMatches(Token matchToken) {
        return nextIfMatches(matchToken, null);
    }

    public Optional<Token> nextIfMatches(Token.TokenType matchType) {
        return nextIfMatches(Token.withType(matchType), null);
    }

    public Optional<Token> nextIfMatches(Token.TokenType matchType, String errorMessage) {
        return nextIfMatches(Token.withType(matchType), errorMessage);
    }

    @Override
    public void forEachRemaining(Consumer<? super Token> action) {
        tokenList.stream().skip(tokenIndex).forEach(action);
    }

    public void assertNext(Token.TokenType type, String errorMessage) {
        if(peek().isNot(type)) {
            Util.emitFatalError(errorMessage);
        }
    }

    public void assertNextAndAdvance(Token.TokenType type, String errorMessage) {
        if(peek().isNot(type)) {
            Util.emitFatalError(errorMessage);
        }

        advance();
    }

    public List<Token> readCurrentLevel(Token.TokenType openType, Token.TokenType closeType) {
        return ParserUtils.readCurrentLevel(this, openType, closeType);
    }
}
