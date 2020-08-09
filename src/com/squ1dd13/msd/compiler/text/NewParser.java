package com.squ1dd13.msd.compiler.text;

import com.squ1dd13.msd.compiler.constructs.*;
import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.compiler.text.lexer.Token.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;
import java.util.stream.*;

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

        public List<Match> matchesInList(List<Token> tokens) {
            List<Match> matches = new ArrayList<>();

            for(int i = 0; i < tokens.size(); ++i) {
                int j = 0;
                for(; j < patternTokens.size() && (i + j) < tokens.size(); ++j) {
                    if(!tokenMatches(patternTokens.get(j), tokens.get(i + j))) break;
                }

                if(j == patternTokens.size()) {
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

    public static List<List<Token>> splitTokens(List<Token> tokens, TokenType delim) {
        List<List<Token>> tokenLists = new ArrayList<>();

        List<Token> currentList = new ArrayList<>();
        for(Token tkn : tokens) {
            if(tkn.is(delim)) {
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
            if(TokenPattern.tokenMatches(delim, tkn)) {
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

//    public static List<List<Token>>

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

    public static List<Token> filterBlankTokens(Collection<Token> tokens) {
        return tokens.stream().filter(
            t -> t.isNot(TokenType.Whitespace) && t.isNot(TokenType.Newline)
        ).collect(Collectors.toList());
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

    public ParsedCommand readCommand() {
        Token nameToken = readNotBlank();
        System.out.println("command: " + nameToken.getText());

        Token firstBracket = readNotBlank();

        if(!firstBracket.is(TokenType.OpenBracket)) {
            System.out.println("Not a bracket");
        }

        List<Token> argTokens = new ArrayList<>();
        while(peekNotBlank().isNot(TokenType.CloseBracket)) {
            argTokens.add(readNotBlank());
        }

        ParsedCommand command = new ParsedCommand();
        command.nameToken = nameToken;

        List<List<Token>> argumentLists = splitTokens(argTokens, TokenType.Comma);
        for(var argList : argumentLists) {
            if(argList.size() != 1) {
                Util.emitFatalError("Invalid argument in call to '" + nameToken.getText() + "'");
            }

            command.argumentTokens.add(argList.get(0));
        }

        readNext();

        return command;
    }

    public ParsedConditional parseIf() {
        return new ParsedConditional(tokenList);
    }

    public List<Compilable> parseTokens() {
        List<Token> cleanTokens = filterBlankTokens(tokenList);

        List<ParsedConditional> removedConditionals = new ArrayList<>();

        List<Token> pureStatements = new ArrayList<>();
        tokenList = cleanTokens;
        for(index = 0; index < cleanTokens.size(); ++index) {
            if(peek().getText().equals("if")) {
                int conditionalIndex = removedConditionals.size();
                removedConditionals.add(parseIf());

                pureStatements.add(
                    Token.withType(TokenType.None).withInt(conditionalIndex)
                );

                pureStatements.add(Token.withType(TokenType.Semicolon));
                index += removedConditionals.get(conditionalIndex).tokenLength;
            } else {
                pureStatements.add(peek());
            }
        }

        cleanTokens = pureStatements;

        index = 0;
        List<List<Token>> statements = splitTokens(cleanTokens, TokenType.Semicolon);

        List<Compilable> elements = new ArrayList<>();
        for(List<Token> statement : statements) {
            tokenList = statement;
            index = 0;

            System.out.println(statement.get(0).type);

            if(statement.get(0).type == TokenType.None) {
                elements.add(removedConditionals.get(statement.get(0).getInteger()).toCompilable());
                continue;
            }

            ParsedCommand command = readCommand();
            System.out.println(command.nameToken);

            elements.add(command.toCompilable());
        }

        return elements;
    }
}
