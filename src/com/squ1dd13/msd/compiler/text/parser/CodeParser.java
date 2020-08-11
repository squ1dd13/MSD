package com.squ1dd13.msd.compiler.text.parser;

import com.squ1dd13.msd.compiler.*;
import com.squ1dd13.msd.compiler.language.*;
import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

public class CodeParser extends GenericParser {
    public CodeParser(Iterator<Token> tokenIterator) {
        super(tokenIterator);
    }

    public CodeParser(List<Token> tokenList) {
        super(tokenList);
    }

    /*
    private final List<IdentifierChanger> identifierChangers = new ArrayList<>();

    private BasicCommand nextCommand(Token nameToken) {
        var statementTokens = readStatement(tokenIterator);

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
            if(tokens.get(i).is(Token.TokenType.IdentifierOrKeyword)) {
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

        var argumentListTokens = readCurrentLevel(iterator, Token.TokenType.OpenBracket, Token.TokenType.CloseBracket);

        // FIXME: splitTokens doesn't care about levels, so nested calls won't always work.
        var separateArguments = splitTokens(argumentListTokens, Token.TokenType.Comma);

        for(int i = 0; i < separateArguments.size(); ++i) {
            List<Token> argumentTokens = filterBlankTokens(separateArguments.get(i));

            LowLevelType argumentType = CommandRegistry.get(opcode).lowLevelParameters().get(i);

            if(argumentTokens.size() > 1) {
                // See if we can evaluate these tokens statically (for mathematical expressions).
                // First, check if all the tokens are mathematical.
                if(argumentTokens.stream().allMatch(token ->
                    token.is(Token.TokenType.Operator)
                        || token.is(Token.TokenType.FloatLiteral)
                        || token.is(Token.TokenType.IntLiteral))) {

                    var postfixTokens = ArithmeticConverter.infixToPostfix2(argumentTokens);
                    Optional<Double> result = ArithmeticConverter.solve(postfixTokens);

                    if(result.isPresent()) {
                        Token resultToken = new Token();
                        if(argumentType == LowLevelType.F32) {
                            float floatResult = (float)result.get().doubleValue();
                            resultToken = Token.withType(Token.TokenType.FloatLiteral).withFloat(floatResult);
                        } else if(argumentType.highLevelType().isInteger()) {
                            int intResult = (int)result.get().doubleValue();
                            resultToken = Token.withType(Token.TokenType.IntLiteral).withInt(intResult);
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

    public List<Compilable> parseCodeStructures() {
        List<Compilable> parsedObjects = new ArrayList<>();

        while(tokenIterator.hasNext()) {
            Token token = tokenIterator.next();

            if(token.is(Token.TokenType.IdentifierOrKeyword) && token.getText().equals("if")) {
                parsedObjects.add(readConditional());
            } else {
                var command = readNextCommand(token);
                if(command != null) parsedObjects.add(command);
            }
        }

        return parsedObjects;
    }

     */
}
