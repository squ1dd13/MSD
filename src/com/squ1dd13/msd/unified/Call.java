package com.squ1dd13.msd.unified;

import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.compiler.text.lexer.Token.*;
import com.squ1dd13.msd.compiler.text.parser.*;
import com.squ1dd13.msd.decompiler.shared.*;
import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.util.*;

public class Call implements ScriptElement {
    private String name;
    private List<ScriptValue> arguments;
    private int opcode;

    public Call(RandomAccessFile randomAccessFile) throws IOException {
        opcode = randomAccessFile.read() | randomAccessFile.read() << 8;

        if(opcode == 0) {
            System.out.println("invalid opcode");
            name = "unknown";
            arguments = new ArrayList<>();
        }

        var commandEntry = CommandRegistry.getOptional(opcode);
        if(commandEntry.isEmpty()) {
            commandEntry = CommandRegistry.getOptional(opcode & 0xFFF);
        }

        if(commandEntry.isPresent()) {
            name = commandEntry.get().name;
            arguments = new ArrayList<>();

            int argCount = commandEntry.get().parameterTypes.size();
            for(int i = 0; i < argCount; ++i) {
                arguments.add(new ScriptValue(randomAccessFile));
            }
        } else {
            System.out.println("unknown opcode");
        }
    }

    public Call() {

    }

    public static Optional<Call> parse(TokenIterator tokenIterator) {
        Call call = new Call();
        call.arguments = new ArrayList<>();

        Token nameToken = tokenIterator.peek();

        if(nameToken.is(TokenType.IdentifierOrKeyword)) {
            call.name = nameToken.getText();
            tokenIterator.advance();
        } else {
            return Optional.empty();
        }

        if(tokenIterator.peek().isNot(TokenType.OpenParen)) return Optional.empty();
        tokenIterator.advance();

        List<Token> argumentTokens = tokenIterator.readCurrentLevel(TokenType.OpenParen, TokenType.CloseParen);
        var groups = ParserUtils.splitCurrentLevel(argumentTokens,
            TokenType.OpenParen,
            TokenType.CloseParen,
            TokenType.Comma
        );

        for(List<Token> argumentGroup : groups) {
            Optional<ScriptValue> argumentOptional = ScriptValue.parse(new TokenIterator(argumentGroup));

            if(argumentOptional.isPresent()) {
                call.arguments.add(argumentOptional.get());
            } else {
                System.out.println("invalid argument");
                return Optional.empty();
            }
        }

        return Optional.of(call);
    }

    public String getName() {
        return name;
    }

    public int getOpcode() {
        return opcode;
    }

    private void addArgumentTypes() {
        var commandEntry = CommandRegistry.getOptional(name);
        if(commandEntry.isPresent()) {
            var concreteTypes = commandEntry.get().concreteParamTypes();

            if(concreteTypes.size() != arguments.size()) {
                System.out.println("argument/parameter count mismatch");
            }

            for(int i = 0; i < arguments.size() && i < concreteTypes.size(); i++) {
                var type = concreteTypes.get(i);
                if(type == ConcreteType.Unknown) {
                    continue;
                }

                ScriptValue argument = arguments.get(i);
                argument.concreteType = concreteTypes.get(i);
            }
        } else {
            System.out.println("no command");
        }
    }

    @Override
    public List<Integer> toBytes(Context context) {
        addArgumentTypes();

        var commandEntry = CommandRegistry.getOptional(name);
        if(commandEntry.isPresent()) {
            List<Integer> bytes = new ArrayList<>(List.of(commandEntry.get().getOpcode()));

            for(var argument : arguments) {
                bytes.addAll(argument.toBytes(context));
            }

            return bytes;
        }

        return new ArrayList<>();
    }

    @Override
    public int getLength() {
        int totalLength = 1;

        for(var arg : arguments) {
            totalLength += arg.getLength();
        }

        return totalLength;
    }

    @Override
    public String toString() {
        List<String> argStrings = new ArrayList<>();
        for(var arg : arguments) {
            argStrings.add(arg.toString());
        }

        return name + "(" + String.join(", ", argStrings) + ")";
    }
}
