package com.squ1dd13.msd.compiler.text.parser;

import com.squ1dd13.msd.compiler.*;
import com.squ1dd13.msd.compiler.language.*;
import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

public class ParsedCommand implements CanBeCompilable {
    public Token nameToken;
    public List<Token> argumentTokens = new ArrayList<>();

    @Override
    public Compilable toCompilable() {
        var name = nameToken.getText();
        int opcode = CommandRegistry.opcodeForName(name);

        BasicCommand command = BasicCommand.create(
            opcode,
            name
        );

        if(command.highLevel.opcode == -1) {
            Util.emitFatalError("Unknown command '" + name + "'");
        }

        for(int i = 0; i < argumentTokens.size(); ++i) {
            Token token = argumentTokens.get(i);

            Argument arg = null;

            var realType = CommandRegistry.get(opcode).lowLevelParameters().get(i);

            final String argumentErrorPrefix = "Argument " + (i + 1) + " for " + name;
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
                    System.out.println("passing offset as " + realType);
                    arg = new Argument(realType, Integer.parseInt(offsetString));
                    break;

                case StringLiteral:
                    arg = new Argument(realType, token.getText());
                    break;
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
