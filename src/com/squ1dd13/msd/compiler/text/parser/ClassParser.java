package com.squ1dd13.msd.compiler.text.parser;

import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.decompiler.high.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

public class ClassParser extends ObjectParser {
    public final Map<Integer, MethodParser> parsedMethods = new HashMap<>();
    public String name;
    public boolean isStatic;

    public ClassParser(Iterator<Token> iterator) {
        var first = iterator.next();

        if(first.getText().equals("static")) {
            isStatic = true;
            iterator.next();
        }

        name = iterator.next().getText();

        var openBrace = iterator.next();
        if(openBrace.isNot(Token.TokenType.OpenBrace)) {
            Util.emitFatalError("Class declarations should be written '(static) class XYZ { ... }'");
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
            if(method.parameters.size() != command.arguments.length - (method.isStatic ? 0 : 1)) {
                continue;
            }

            if(!method.isStatic && command.arguments[0].type.isVariable()) {
                // Use the parameter to give the variable a more meaningful name.
                var variable = Variable.getOrCreate(command.arguments[0]);
                variable.customTypeName = name;
                variable.setCustomName(null);
            }

            // Static classes don't have a hidden "this" parameter, so the arguments start at 0.
            int i = method.isStatic ? 0 : 1;
            for(; i < command.arguments.length; ++i) {
                int realIndex = method.isStatic ? i : i - 1;
                String paramName = method.parameterNames.get(realIndex);

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
        return method.callString(name, command);
    }

    private void parseMethods() {
        while(tokenIterator.hasNext()) {
            var method = new MethodParser(tokenIterator);
            if(method.name == null) break;

            if(isStatic && method.isStatic) {
                Util.emitWarning("Methods in static classes are implicitly static.");
            }

            method.isStatic |= isStatic;

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
        private Set<Integer> booleanIndices = new HashSet<>();
        public boolean isStatic;

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

        String callString(String className, Command command) {
            if(parameters.size() != command.arguments.length - (isStatic ? 0 : 1)) {
                Util.emitFatalError("Incorrect argument count for method call");
            }

            String receiverString = isStatic ? className : command.arguments[0].toString();
            StringBuilder callBuilder = new StringBuilder(receiverString).append('.').append(name).append('(');

            // The first argument is the receiver unless this is a static class.
            int i = isStatic ? 0 : 1;
            for(; i < command.arguments.length; ++i) {
                int realIndex = isStatic ? i : i - 1;

                String paramName = parameterNames.get(realIndex);
                String argumentString = command.argumentString(i);

                if(booleanIndices.contains(realIndex)) {
                    int integer = command.arguments[i].intValue;

                    if(integer == 0) {
                        argumentString = "false";
                    } else if(integer == 1) {
                        argumentString = "true";
                    }
                }

                callBuilder.append(paramName).append(": ").append(argumentString);

                if(i != command.arguments.length - 1) {
                    callBuilder.append(", ");
                }
            }

            return callBuilder.append(")").toString();
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

            if(startTokens.size() == 3) {
                isStatic = startTokens.get(0).getText().equals("static");
                startTokens.remove(0);
            } else if(startTokens.size() != 2) {
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

            parameterNames = new ArrayList<>(Arrays.asList(new String[paramGroups.size()]));

            boolean didSpecifyIndex = false;

            for(int i = 0; i < paramGroups.size(); i++) {
                List<Token> group = paramGroups.get(i);

                if(group.size() != 2 && (group.size() != 4 || group.get(2).isNot(Token.TokenType.Colon))) {
                    Util.emitFatalError("Method parameters should be written as 'SomeType paramName'" +
                        "or 'SomeType paramName:index' (e.g. 'SomeType paramName:0').");
                }

                String typeName = group.get(0).getText();
                String paramName = group.get(1).getText();

                int paramIndex = i;
                if(group.size() == 4) {
                    paramIndex = group.get(3).getInteger();

                    if(paramIndex >= paramGroups.size()) {
                        Util.emitFatalError("Invalid index '" + paramIndex + "'");
                    }

                    if(i != 0 && !didSpecifyIndex) {
                        Util.emitFatalError("If one parameter's index is specified, all other parameters' indices must be too.");
                    }

                    didSpecifyIndex = true;
                } else if(i != 0 && didSpecifyIndex) {
                    Util.emitFatalError("All indices must be given.");
                }

                AbstractType paramType;
                if(typeName.equals("Bool")) {
                    paramType = AbstractType.Int;
                    booleanIndices.add(paramIndex);
                } else {
                    paramType = AbstractType.valueOf(typeName);
                }

                parameterNames.add(paramIndex, paramName);
                parameters.put(paramName, paramType);
            }
        }
    }

}
