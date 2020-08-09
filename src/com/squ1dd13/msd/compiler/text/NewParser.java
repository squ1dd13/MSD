package com.squ1dd13.msd.compiler.text;

import com.squ1dd13.msd.compiler.constructs.*;
import com.squ1dd13.msd.compiler.text.Lexer.Token.*;

import java.util.*;

import static com.squ1dd13.msd.compiler.text.Lexer.*;

public class NewParser {
    public NewParser(List<Token> tokens) {
        tokenList = tokens;
    }

    public static class TokenPattern {
        public static class Match {
            public List<Token> matchedTokens = new ArrayList<>();

            public Match(List<Token> matched) {
                matchedTokens = matched;
            }
        }

        private final List<Token> patternTokens;

        public TokenPattern(Token ...tokens) {
            patternTokens = Arrays.asList(tokens);
        }

        private boolean tokenMatches(Token patternToken, Token actualToken) {
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

            return false;
        }

        public List<Match> matchesInList(List<Token> tokens) {
            List<Match> matches = new ArrayList<>();

            for(int i = 0; i < tokens.size(); ++i) {
                int j = 0;
                for(; j < patternTokens.size() && (i + j) < tokens.size(); ++j) {
                    if(!tokenMatches(patternTokens.get(j), tokens.get(i + j))) break;
                }

                if(j == tokens.size()) {
                    matches.add(new Match(tokens.subList(i, j)));
                }
            }

            return matches;
        }
    }

    private static Set<String> keywords = Set.of(
        "and",
        "or",
        "if_", // TODO: Make 'if' command illegal once conditionals work.
        "while"
    );

    private List<Token> tokenList;
    private int index;

    private Token peek() {
        return tokenList.get(index);
    }

    private Token peekNotBlank() {
        for(int i = 0; index + i < tokenList.size(); ++i) {
            Token t = tokenList.get(index + i);

            if(t.isNot(TokenType.Whitespace) && t.isNot(TokenType.Newline)) {
                return t;
            }
        }

        return null;
    }

    private Token readNext() {
        return tokenList.get(index++);
    }

    private Token readNotBlank() {
        skipBlank();
        return readNext();
    }

    private void skip(TokenType ...skipTypes) {
        List<TokenType> skipping = Arrays.asList(skipTypes);
        while(skipping.contains(peek().type)) index++;
    }

    private void skipBlank() {
        skip(TokenType.Whitespace, TokenType.Newline);
    }

    public List<Token> readCommand() {
        Token nameToken = readNotBlank();
        System.out.println("command: " + nameToken.getText());

        Token firstBracket = readNotBlank();

        if(!firstBracket.is(TokenType.OpenBracket)) {
            System.out.println("Not a bracket");
        }

        List<Token> argTokens = new ArrayList<>();
        while(peekNotBlank().isNot(TokenType.CloseBracket)) {
            Token next = readNotBlank();
            System.out.println("arg: " + next);

            argTokens.add(next);
        }

        System.out.println("end");
        Token endBracket = readNotBlank();

        List<Token> commandTokens = new ArrayList<>(List.of(nameToken, firstBracket));
        commandTokens.addAll(argTokens);
        commandTokens.add(endBracket);

        return commandTokens;
    }

    public List<Compilable> parseTokens() {
        for(index = 0; index < tokenList.size(); ++index) {
            Token token = tokenList.get(index);

            if(token.type == TokenType.IdentifierOrKeyword) {
                String text = token.getText();

                if(!keywords.contains(text)) {
                    // Command name (hopefully).

                }
            }
        }

        return null;
    }
}
