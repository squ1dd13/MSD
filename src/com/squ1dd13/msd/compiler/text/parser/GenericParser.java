package com.squ1dd13.msd.compiler.text.parser;

import com.squ1dd13.msd.compiler.text.lexer.*;

import java.util.*;
import java.util.stream.*;

// Base parser class.
public class GenericParser {
    public final Iterator<Token> tokenIterator;

    public GenericParser(Iterator<Token> tokenIterator) {
        this.tokenIterator = tokenIterator;
    }

    public GenericParser(List<Token> tokenList) {
        tokenIterator = filterBlankTokens(tokenList).iterator();
    }

    public static boolean tokenMatches(Token patternToken, Token actualToken) {
        if(patternToken == null) return true;

        if(patternToken.hasText && actualToken.hasText) {
            return patternToken.getText().equals(actualToken.getText());
        }

        if(patternToken.hasFloat && actualToken.hasFloat) {
            return patternToken.getFloat() == actualToken.getFloat();
        }

        if(patternToken.hasInt && actualToken.hasInt) {
            return patternToken.getInteger() == actualToken.getInteger();
        }

        return patternToken.type == actualToken.type;
    }

    public static List<List<Token>> splitTokens(List<Token> tokens, Token.TokenType... delims) {
        List<Token.TokenType> delimiters = Arrays.asList(delims);

        List<List<Token>> tokenLists = new ArrayList<>();

        List<Token> currentList = new ArrayList<>();
        for(Token tkn : tokens) {
            if(delimiters.contains(tkn.type)) {
                tokenLists.add(new ArrayList<>(currentList));
                currentList.clear();
            } else {
                currentList.add(tkn);
            }
        }

        if(currentList.isEmpty()) {
            return tokenLists;
        }

        tokenLists.add(currentList);

        return tokenLists;
    }

    public static List<List<Token>> splitTokens(List<Token> tokens, Token delim) {
        List<List<Token>> tokenLists = new ArrayList<>();

        List<Token> currentList = new ArrayList<>();
        for(Token tkn : tokens) {
            if(tokenMatches(delim, tkn)) {
                tokenLists.add(new ArrayList<>(currentList));
                currentList.clear();
            } else {
                currentList.add(tkn);
            }
        }

        if(currentList.isEmpty()) {
            return tokenLists;
        }

        tokenLists.add(currentList);

        return tokenLists;
    }

    public static List<Token> filterBlankTokens(Collection<Token> tokens) {
        return tokens.stream().filter(
            t -> t.isNot(Token.TokenType.Whitespace) && t.isNot(Token.TokenType.Newline)
        ).collect(Collectors.toList());
    }

    private static List<Token> readCurrentLevel(Iterator<Token> iterator, Token.TokenType open, Token.TokenType close) {
        // Assume the current level is 1.
        int level = 1;

        List<Token> levelTokens = new ArrayList<>();

        while(iterator.hasNext()) {
            Token t = iterator.next();

            if(t.is(open)) {
                level++;
            } else if(t.is(close)) {
                level--;
            }

            if(level == 0) break;
            levelTokens.add(t);
        }

        return levelTokens;
    }
}
