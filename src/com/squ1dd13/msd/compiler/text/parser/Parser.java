package com.squ1dd13.msd.compiler.text.parser;

import com.squ1dd13.msd.compiler.*;
import com.squ1dd13.msd.compiler.language.*;
import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.compiler.text.lexer.Token.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;
import java.util.stream.*;

// TODO: Completely restructure parser.
public class Parser {
    private static final List<IdentifierChanger> identifierChangers = new ArrayList<>();
    private List<Token> tokenList;
    private int index;

    // TODO: Make 'if' command illegal.

    public Parser(List<Token> tokens) {
        tokenList = filterBlankTokens(tokens);
    }

    // Find all labels in a list of tokens. This should be done before
    //  parsing any commands, because goto() and suchlike may rely on labels.
    private static void findLabels(List<Token> tokens) {
        // Start at index 1 so we can always look behind.
        for(int i = 1; i < tokens.size(); ++i) {
            if(tokens.get(i).is(TokenType.Colon)) {
                // The previous token was the label name.
                Token labelNameToken = tokens.get(i - 1);

                if(!labelNameToken.is(TokenType.IdentifierOrKeyword)) {
                    Util.emitFatalError("Label name must be a valid identifier");
                }

                // TODO: Finish implementing labels
            }
        }
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

    public static List<List<Token>> splitTokens(List<Token> tokens, TokenType... delims) {
        List<TokenType> delimiters = Arrays.asList(delims);

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
            t -> t.isNot(TokenType.Whitespace) && t.isNot(TokenType.Newline)
        ).collect(Collectors.toList());
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

    private static Token preprocessIdentifier(Token t) {
        Token currentValue = Token.withType(t.type).withText(t.getText());

        for(IdentifierChanger idChanger : identifierChangers) {
            currentValue = idChanger.modifyIdentifier(currentValue);
        }

        return currentValue;
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

    private static List<Token> readStatement(Iterator<Token> iterator) {
        List<Token> statement = new ArrayList<>();

        while(iterator.hasNext()) {
            Token token = iterator.next();

            if(token.is(TokenType.Semicolon)) break;
            statement.add(token);
        }

        return statement;
    }

    private static void preprocess(List<Token> tokens) {
        for(int i = 0; i < tokens.size(); ++i) {
            if(tokens.get(i).isNot(TokenType.IdentifierOrKeyword)) continue;
            tokens.set(i, preprocessIdentifier(tokens.get(i)));
        }
    }

    private static BasicCommand readNextCommand(Token nameToken, Iterator<Token> iterator) {
        var statementTokens = readStatement(iterator);

        var allTokens = new ArrayList<>(List.of(nameToken));
        allTokens.addAll(statementTokens);

        final Set<String> usingKeywords = Set.of("using", "global");
        if(usingKeywords.contains(nameToken.getText())) {
            identifierChangers.add(new UsingStatement(allTokens));

            return null;
        }

        preprocess(allTokens);
        return parseSingleCommand(allTokens);
    }

    private static Argument tokenToArgument(Token token, int i, LowLevelType realType) {
        Argument arg = null;

        final String argumentErrorPrefix = "Argument " + (i + 1) + " for command";
        switch(token.type) {
            case FloatLiteral:
                if(realType.highLevelType() == AbstractType.Int) {
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
        for(int i = 0; i < tokens.size(); ++i) {
            if(tokens.get(i).is(TokenType.IdentifierOrKeyword)) {
                tokens.set(i, preprocessIdentifier(tokens.get(i)));
            }
        }

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

            LowLevelType argumentType = CommandRegistry.get(opcode).lowLevelParameters().get(i);

            if(argumentTokens.size() > 1) {
                // See if we can evaluate these tokens statically (for mathematical expressions).
                // First, check if all the tokens are mathematical.
                if(argumentTokens.stream().allMatch(token ->
                    token.is(TokenType.Operator)
                        || token.is(TokenType.FloatLiteral)
                        || token.is(TokenType.IntLiteral))) {

                    var postfixTokens = ArithmeticConverter.infixToPostfix2(argumentTokens);
                    Optional<Double> result = ArithmeticConverter.solve(postfixTokens);

                    if(result.isPresent()) {
                        Token resultToken = new Token();
                        if(argumentType == LowLevelType.F32) {
                            float floatResult = (float)result.get().doubleValue();
                            resultToken = Token.withType(TokenType.FloatLiteral).withFloat(floatResult);
                        } else if(argumentType.highLevelType().isInteger()) {
                            int intResult = (int)result.get().doubleValue();
                            resultToken = Token.withType(TokenType.IntLiteral).withInt(intResult);
                        } else {
                            Util.emitFatalError("Cannot evaluate mathematical expression when parameter is non-numeric");
                        }

                        argumentTokens = new ArrayList<>(List.of(resultToken));
                    }
                } else {
                    System.out.println("couldn't evaluate");
                }
            }

            var arg = tokenToArgument(argumentTokens.get(0), i, argumentType);
            command.arguments.add(arg);
        }

        return command;
    }

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
                var command = readNextCommand(token, iterator);
                if(command != null) parsedObjects.add(command);
            }
        }

        return parsedObjects;
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
