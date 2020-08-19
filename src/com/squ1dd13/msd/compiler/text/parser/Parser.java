package com.squ1dd13.msd.compiler.text.parser;

import com.squ1dd13.msd.compiler.*;
import com.squ1dd13.msd.compiler.language.*;
import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.compiler.text.lexer.Token.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;
import java.util.concurrent.atomic.*;

public class Parser {
    private static final List<IdentifierChanger> identifierChangers = new ArrayList<>();
    private static final Map<String, TypedToken> typedTokens = new HashMap<>();
    private List<Token> tokenList;
    private int index;
    private static final Set<String> procedureNames = new HashSet<>();

    // TODO: Make 'if' command illegal.

    public Parser(List<Token> tokens) {
        tokenList = ParserUtils.filterBlankTokens(tokens);
    }

    private static Token preprocessIdentifier(Token t) {
        TypedToken currentValue = TypedToken.wrap(Token.withType(t.type).withText(t.getText()));

        for(IdentifierChanger idChanger : identifierChangers) {
            currentValue = idChanger.modifyIdentifier(currentValue.token);
        }

        if(currentValue.type != null) typedTokens.put(currentValue.token.getText(), currentValue);

        return currentValue.token;
    }

    private static Conditional readConditional(Iterator<Token> iterator, boolean isElse) {
        // TODO: Check token types to enforce syntax.

        List<Token> conditionTokens = new ArrayList<>();
        if(!isElse) {
            // The 'if' will have already been read, so we start with a bracket.
            iterator.next();

            // Read the contents of the brackets.
            conditionTokens = ParserUtils.readCurrentLevel(iterator, TokenType.OpenParen, TokenType.CloseParen);
        }

        // Skip the opening brace.
        iterator.next();

        // Read the contents of the braces.
        var bodyTokens = ParserUtils.readCurrentLevel(iterator, TokenType.OpenBrace, TokenType.CloseBrace);

        Conditional realConditional = new Conditional();
        realConditional.isElse = isElse;
        realConditional.mainBodyElements = new ArrayList<>();

        var basicCommands = new Parser(bodyTokens).parseTokens();//parseCommandTokens();
        realConditional.mainBodyElements.addAll(basicCommands);

        if(!isElse) {
            var conditionCommands = parseSingleCommand(conditionTokens);
            realConditional.conditions = new ArrayList<>(List.of(conditionCommands));
        }


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

        final Set<String> usingKeywords = Set.of("using", "global", "local");
        if(usingKeywords.contains(nameToken.getText())) {
            identifierChangers.add(new UsingStatement(allTokens));

            return null;
        }

        preprocess(allTokens);
        return parseSingleCommand(allTokens);
    }

    private static Argument tokenToArgument(Token token, int i, ConcreteType realType) {
        Argument arg = null;

        final String argumentErrorPrefix = "Argument " + (i + 1) + " for command";
        switch(token.type) {
            case FloatLiteral:
                if(realType.highLevelType() == AbstractType.Int) {
                    Util.emitFatalError(
                        argumentErrorPrefix + " should be an integer value, but a float was passed"
                    );
                }

                if(realType != ConcreteType.F32) {
                    Util.emitFatalError(
                        argumentErrorPrefix + " must be of type " + realType.highLevelType().toString()
                    );
                }

                arg = new Argument(ConcreteType.F32, token.getFloat());
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

    private static Token createResultToken(double result, ConcreteType targetType) {
        Token resultToken = new Token();

        if(targetType == ConcreteType.F32) {
            float floatResult = (float)result;
            resultToken = Token.withType(TokenType.FloatLiteral).withFloat(floatResult);
        } else if(targetType.highLevelType().isInteger()) {
            int intResult = (int)result;
            resultToken = Token.withType(TokenType.IntLiteral).withInt(intResult);
        } else {
            Util.emitFatalError("Cannot evaluate mathematical expression when parameter is non-numeric");
        }

        return resultToken;
    }

    public static BasicCommand parseSingleCommand(List<Token> tokens) {
        for(int i = 0; i < tokens.size(); ++i) {
            if(tokens.get(i).is(TokenType.IdentifierOrKeyword)) {
                tokens.set(i, preprocessIdentifier(tokens.get(i)));
            }
        }

        var iterator = tokens.iterator();

        String name = iterator.next().getText();
        AtomicInteger opcode = new AtomicInteger(CommandRegistry.opcodeForName(name));

        // Read the bracket.
        iterator.next();

        var argumentListTokens = ParserUtils.readCurrentLevel(iterator, TokenType.OpenParen, TokenType.CloseParen);

        if(opcode.get() == -1 && name.contains(".")) {
            int dotIndex = name.indexOf('.');

            String className = name.substring(0, dotIndex);
            String methodName = name.substring(dotIndex + 1);

            // Possibly a method call.
            ClassRegistry.getClass(className).ifPresentOrElse(classParser -> {
                classParser.getMethod(methodName).ifPresentOrElse(methodParser -> {
                    opcode.set(methodParser.opcode);
                }, () -> {
                    Util.emitFatalError("Class '" + className + "' has no static method named '" + methodName + "'.");
                });
            }, () -> {
//
                // Check for an instance method call.
                Token receiverToken = Token.withType(TokenType.IdentifierOrKeyword).withText(className);
                Token preprocessedReceiver = preprocessIdentifier(receiverToken);
                TypedToken typedToken = typedTokens.get(preprocessedReceiver.getText());

                if(typedToken == null || typedToken.type.isNull()) {
                    Util.emitFatalError("No known class '" + className + "'");
                }

                ClassRegistry.getClass(typedToken.type.getClassName()).ifPresent(classParser -> {
                    classParser.getMethod(methodName).ifPresentOrElse(methodParser -> {
                        opcode.set(methodParser.opcode);

                        // Add the receiver as an argument.
                        argumentListTokens.add(0, Token.withType(TokenType.Comma));
                        argumentListTokens.add(0, typedToken.token);
                    }, () -> {
                        Util.emitFatalError("Class '" + className + "' has no static method named '" + methodName + "'.");
                    });
                });
            });
        }

        if(name.startsWith("proc_")) {
            int offset = -Integer.parseInt(name.split("_")[1]);
            return BasicCommand.create(Opcode.Call.get(), "gosub", new Argument(ConcreteType.S32, offset));
        }

        BasicCommand command = BasicCommand.create(
            opcode.get(),
            name
        );

        command.arguments = new ArrayList<>();

        // FIXME: splitTokens doesn't care about levels, so nested calls won't always work.
        var separateArguments = ParserUtils.splitTokens(argumentListTokens, TokenType.Comma);

        for(int i = 0; i < separateArguments.size(); ++i) {
            List<Token> argumentTokens = ParserUtils.filterBlankTokens(separateArguments.get(i));

            ConcreteType argumentType = CommandRegistry.get(opcode.get()).concreteParamTypes().get(i);

            if(argumentTokens.size() > 2) {
                if(argumentTokens.get(1).is(TokenType.Colon)) {
                    argumentTokens = argumentTokens.subList(2, argumentTokens.size());
                }
            } else if(argumentTokens.size() > 1) {
                // See if we can evaluate these tokens statically (for mathematical expressions).
                // First, check if all the tokens are mathematical.
                if(argumentTokens.stream().allMatch(token ->
                    token.is(TokenType.Operator)
                        || token.is(TokenType.FloatLiteral)
                        || token.is(TokenType.IntLiteral))) {

                    var postfixTokens = ArithmeticConverter.infixToPostfix2(argumentTokens);
                    Optional<Double> result = ArithmeticConverter.solve(postfixTokens);

                    if(result.isPresent()) {
                        Token resultToken = createResultToken(result.get(), argumentType);
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

    public List<Compilable> parseTokens() {
        // To make programming easier, statements are separated by semicolons (';').
        // Whitespace actually has no significance and is just filtered out here.
        tokenList = ParserUtils.filterBlankTokens(tokenList);

        List<Compilable> parsedObjects = new ArrayList<>();

        Iterator<Token> iterator = tokenList.iterator();
        while(iterator.hasNext()) {
            Token token = iterator.next();

            if(token.is(TokenType.IdentifierOrKeyword)) {
                if(token.getText().equals("if")) {
                    parsedObjects.add(readConditional(iterator, false));
                    continue;
                } else if(token.getText().equals("else")) {
                    var last = parsedObjects.get(parsedObjects.size() - 1);
                    if(!(last instanceof Conditional)) {
                        Util.emitFatalError("'else' without 'if'");
                    }

                    // TODO: Change previous if jump.
                    parsedObjects.add(readConditional(iterator, true));
                    continue;
                } else if(token.getText().equals("void")) {
                    var sub = new Subprocedure(iterator);
                    procedureNames.add(sub.name);
                    parsedObjects.add(sub);
                    continue;
                }
            }

            var command = readNextCommand(token, iterator);
            if(command != null) parsedObjects.add(command);
        }

        return parsedObjects;
    }
}
