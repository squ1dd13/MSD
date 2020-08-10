package com.squ1dd13.msd.compiler.text.parser;

import com.squ1dd13.msd.compiler.*;
import com.squ1dd13.msd.compiler.language.*;
import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.compiler.text.lexer.Token.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;
import java.util.stream.*;

public class Parser {
    public Parser(List<Token> tokens) {
        tokenList = filterBlankTokens(tokens);
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

    // TODO: Make 'if' command illegal once conditionals work.

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

    private void skip(TokenType... skipTypes) {
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

        while(peekNotBlank() != null && peekNotBlank().isNot(TokenType.CloseBracket)) {
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

    private static List<Token> readCurrentLevel(Iterator<Token> iterator, TokenType open, TokenType close) {
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

    private static Conditional readConditional(Iterator<Token> iterator) {
        // The 'if' will have already been read, so we start with a bracket.
        Token openBracket = iterator.next();

        // Read the contents of the brackets.
        var conditionTokens = readCurrentLevel(iterator, TokenType.OpenBracket, TokenType.CloseBracket);

        Token openBrace = iterator.next();

        // Read the contents of the braces.
        var bodyTokens = readCurrentLevel(iterator, TokenType.OpenBrace, TokenType.CloseBrace);

        Conditional realConditional = new Conditional();
        realConditional.mainBodyElements = new ArrayList<>();

        var basicCommands = new Parser(bodyTokens).parseTokens2();//parseCommandTokens();
        realConditional.mainBodyElements.addAll(basicCommands);

        var conditionCommands = parseSingleCommand(conditionTokens);
        realConditional.conditions = new ArrayList<>(List.of(conditionCommands));

        return realConditional;
    }

    public List<Compilable> parseTokens2() {
        // To make programming easier, statements are separated by semicolons (';').
        // Whitespace actually has no significance and is just filtered out here.
        tokenList = filterBlankTokens(tokenList);

        List<Compilable> parsedObjects = new ArrayList<>();

        Iterator<Token> iterator = tokenList.iterator();
        while(iterator.hasNext()) {
            Token token = iterator.next();

            if(token.is(TokenType.IdentifierOrKeyword) && token.getText().equals("if")) {
                parsedObjects.add(readConditional(iterator));
            } else {
                parsedObjects.add(readNextCommand(token, iterator));
            }
        }

        return parsedObjects;
    }

    private static List<Token> readStatement(Iterator<Token> iterator) {
        List<Token> statement = new ArrayList<>();

        while(iterator.hasNext()) {
            Token token = iterator.next();

            if(token.is(TokenType.Semicolon)) break;
            statement.add(token);
        }

        return statement;
    }

    private static BasicCommand readNextCommand(Token nameToken, Iterator<Token> iterator) {
        var statementTokens = readStatement(iterator);

        var tkns = new ArrayList<>(List.of(nameToken));
        tkns.addAll(statementTokens);
        return parseSingleCommand(tkns);
    }

    private static Argument tokenToArgument(Token token, int i, LowLevelType realType) {
        Argument arg = null;

        final String argumentErrorPrefix = "Argument " + (i + 1) + " for command";
        switch(token.type) {
            case FloatLiteral:
                if(realType.highLevelType() == HighLevelType.Int) {
                    Util.emitFatalError(
                        argumentErrorPrefix + " should be an integer value, but a float was passed"
                    );
                }

                if(realType != LowLevelType.F32) {
                    Util.emitFatalError(
                        argumentErrorPrefix + " must be of type " + realType.highLevelType().toString()
                    );
                }

                arg = new Argument(LowLevelType.F32, token.getFloat());
                break;

            case IntLiteral:
                arg = new Argument(realType, token.getInteger());
                break;

            case IdentifierOrKeyword:
                if(!token.getText().matches("&?(global|local)[^_]+_\\d+")) {
                    Util.emitFatalError("Variable names are not yet supported");
                }

                String offsetString = token.getText().split("_")[1];
                arg = new Argument(realType, Integer.parseInt(offsetString));
                break;

            case StringLiteral:
                arg = new Argument(realType, token.getText());
                break;
        }

        return arg;
    }

    public static BasicCommand parseSingleCommand(List<Token> tokens) {
        var iterator = tokens.iterator();

        String name = iterator.next().getText();
        int opcode = CommandRegistry.opcodeForName(name);

        BasicCommand command = BasicCommand.create(
            opcode,
            name
        );

        command.arguments = new ArrayList<>();

        // Read the bracket.
        iterator.next();

        var argumentListTokens = readCurrentLevel(iterator, TokenType.OpenBracket, TokenType.CloseBracket);

        // FIXME: splitTokens doesn't care about levels, so nested calls won't always work.
        var separateArguments = splitTokens(argumentListTokens, TokenType.Comma);

        for(int i = 0; i < separateArguments.size(); ++i) {
            List<Token> argumentTokens = filterBlankTokens(separateArguments.get(i));

            if(argumentTokens.size() > 1) {
                System.out.println("Multiple tokens found in argument.");
            }

            LowLevelType argumentType = CommandRegistry.get(opcode).lowLevelParameters().get(i);

            var arg = tokenToArgument(argumentTokens.get(0), i, argumentType);
            command.arguments.add(arg);
        }

        return command;
    }

    public List<BasicCommand> parseCommandTokens() {
        List<BasicCommand> commands = new ArrayList<>();

        Iterator<Token> iterator = tokenList.iterator();
        while(iterator.hasNext()) {
            var statementTokens = readStatement(iterator);
            if(statementTokens.isEmpty()) continue;

            commands.add(parseSingleCommand(statementTokens));
        }

        return commands;
    }

    public List<Compilable> parseTokens() {
        List<Token> cleanTokens = filterBlankTokens(tokenList);

        List<ParsedConditional> removedConditionals = new ArrayList<>();

        List<Token> pureStatements = new ArrayList<>();
        tokenList = cleanTokens;
        for(index = 0; index < cleanTokens.size(); ++index) {
            if(false && peek().getText().equals("if")) {
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
