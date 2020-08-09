package com.squ1dd13.msd.compiler.text;

import com.squ1dd13.msd.compiler.constructs.*;
import com.squ1dd13.msd.compiler.constructs.language.*;
import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.compiler.text.lexer.Token.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

public class ParsedCommand implements CanBeCompilable {
    public Token nameToken;
    public List<Token> argumentTokens = new ArrayList<>();

    @Override
    public Compilable toCompilable() {
        LowLevelCommand command = LowLevelCommand.create(
            CommandInfoDesk.getOpcode(nameToken.getText()),
            nameToken.getText()
        );

        var name = nameToken.getText();
        if(command.command.opcode == -1) {
            Util.emitFatalError("Unknown command '" + name + "'");
        }

        for(int i = 0; i < argumentTokens.size(); ++i) {
            Token token = argumentTokens.get(i);

            Argument arg = null;

            var realType = CommandRegistry.getCommand(command.command.opcode).lowLevelParameters().get(i);

            final String argumentErrorPrefix = "Argument " + (i + 1) + " for " + name;
            if(token.is(TokenType.FloatLiteral)) {
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
            } else if(token.is(TokenType.IntLiteral)) {
                arg = new Argument(realType, token.getInteger());
            } else if(token.is(TokenType.IdentifierOrKeyword)) {
                if(!token.getText().matches("&?(global|local)[^_]+_\\d+")) {
                    Util.emitFatalError("Variable names are not yet supported");
                }

                String offsetString = token.getText().split("_")[1];
                System.out.println("passing offset as " + realType);
                arg = new Argument(realType, Integer.parseInt(offsetString));
            } else if(token.is(TokenType.StringLiteral)) {
                arg = new Argument(realType, token.getText());
            }

            if(arg == null) {
                Util.emitFatalError("Invalid argument passed to '" + nameToken.getText() + "'");
                break;
            }

            command.arguments.add(arg);
        }

        return command;
    }
}
