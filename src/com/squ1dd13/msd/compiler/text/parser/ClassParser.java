package com.squ1dd13.msd.compiler.text.parser;

import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.decompiler.high.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

public class ClassParser extends ObjectParser {
    private final Map<Integer, MethodParser> parsedMethods = new HashMap<>();
    public String name;

    public ClassParser(Iterator<Token> iterator) {
        iterator.next();
        name = iterator.next().getText();

        var openBrace = iterator.next();
        if(openBrace.isNot(Token.TokenType.OpenBrace)) {
            Util.emitFatalError("Class declarations should be written 'class XYZ { ... }'");
        }

        tokens = ParserUtils.readCurrentLevel(iterator, Token.TokenType.OpenBrace, Token.TokenType.CloseBrace);
        tokenIterator = tokens.iterator();

        parseMethods();
    }

    public void addVariableNames(List<Command> commands) {
        for(Command command : commands) {
            if(!parsedMethods.containsKey(command.opcode)) {
                continue;
            }

            MethodParser method = parsedMethods.get(command.opcode);
            if(method.parameters.size() != command.arguments.length - 1) {
                continue;
            }

            if(command.arguments[0].type.isVariable()) {
                // Use the parameter to give the variable a more meaningful name.
                var variable = Variable.getOrCreate(command.arguments[0]);
                variable.customTypeName = name;
                variable.setCustomName(null);
            }

            for(int i = 1; i < command.arguments.length; ++i) {
                String paramName = method.parameterNames.get(i - 1);

                if(command.arguments[i].type.isVariable()) {
                    // Use the parameter to give the variable a more meaningful name.
                    var variable = Variable.getOrCreate(command.arguments[i]);
                    if(variable.customTypeName == null) variable.setCustomName(paramName);
                }
            }
        }
    }

    public String createCallString(Command command) {
        if(!parsedMethods.containsKey(command.opcode)) {
            return null;
        }

        MethodParser method = parsedMethods.get(command.opcode);
        if(method.parameters.size() != command.arguments.length - 1) {
            Util.emitFatalError("Incorrect argument count for method call");
        }

        String receiverString = command.arguments[0].toString();
        StringBuilder callBuilder = new StringBuilder(receiverString).append('.').append(method.name).append('(');

        // Skip the first argument because that's the receiver.
        for(int i = 1; i < command.arguments.length; ++i) {
            String paramName = method.parameterNames.get(i - 1);
            callBuilder.append(paramName).append(": ").append(command.argumentString(i));

            if(i != command.arguments.length - 1) {
                callBuilder.append(", ");
            }
        }

        return callBuilder.append(")").toString();
    }

    private void parseMethods() {
        while(tokenIterator.hasNext()) {
            var method = new MethodParser(tokenIterator);
            if(method.name == null) break;

            parsedMethods.put(method.opcode, method);
        }
    }

    private static class MethodParser {
        private final List<Token> tokens;
        private final Iterator<Token> tokenIterator;
        public String name;
        public int opcode;
        public boolean returnsBool;
        public List<String> parameterNames = new ArrayList<>();
        public Map<String, AbstractType> parameters = new HashMap<>();

        public MethodParser(Iterator<Token> iterator) {
            tokens = ParserUtils.readTo(iterator, Token.TokenType.Semicolon);
            tokenIterator = tokens.iterator();

            if(tokens.size() == 1 && tokens.get(0).is(Token.TokenType.CloseBrace)) {
                name = null;
                return;
            }

            // "[0x123]" -> opcode = 0x123
            parseMeta();

            // "void someMethod" -> returnsBool = false, name = "someMethod"
            parseDeclarationStart();

            // "Int paramA, Bool paramB" -> stuff
            parseParams();
        }

        private void parseMeta() {
            if(tokens.get(0).isNot(Token.TokenType.OpenSquare)) return;

            List<Token> metaTokens = ParserUtils.readTo(tokenIterator, Token.TokenType.CloseSquare);
            if(metaTokens.size() != 2) {
                Util.emitFatalError("Invalid meta expression in method declaration");
            }

            opcode = metaTokens.get(1).getInteger();
        }

        private void parseDeclarationStart() {
            List<Token> startTokens = ParserUtils.readTo(tokenIterator, Token.TokenType.OpenParen);

            // We want 2 tokens: a return type and the name ("void xyz").
            if(startTokens.size() != 2) {
                Util.emitFatalError("Invalid method declaration");
            }

            String returnTypeName = startTokens.get(0).getText();

            final Set<String> validReturnTypes = Set.of("void", "bool");
            if(!validReturnTypes.contains(returnTypeName)) {
                Util.emitFatalError("'" + returnTypeName + "' is not a valid method return type");
            }

            returnsBool = returnTypeName.equals("bool");
            name = startTokens.get(1).getText();
        }

        private void parseParams() {
            List<Token> paramTokens = ParserUtils.readTo(tokenIterator, Token.TokenType.CloseParen);
            List<List<Token>> paramGroups = ParserUtils.splitTokens(paramTokens, Token.TokenType.Comma);

            for(var group : paramGroups) {
                if(group.size() != 2) {
                    Util.emitFatalError("Method parameters should be written as \"Type name\".");
                }

                String typeName = group.get(0).getText();
                String paramName = group.get(1).getText();

                AbstractType paramType;
                if(typeName.equals("Bool")) {
                    paramType = AbstractType.Int;
                } else {
                    paramType = AbstractType.valueOf(typeName);
                }

                parameterNames.add(paramName);
                parameters.put(paramName, paramType);
            }
        }
    }

}
