package com.squ1dd13.msd.compiler.text.parser;

import com.squ1dd13.msd.compiler.*;
import com.squ1dd13.msd.compiler.language.*;
import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

import static com.squ1dd13.msd.compiler.text.lexer.Token.TokenType.*;

public class CommandParser {
    private BasicCommand command;

    public CommandParser(TokenIterator tokenIterator) {
        var next = tokenIterator.peek();

        if(next.is(IdentifierOrKeyword)) {
            String name = next.getText();

            if(CommandRegistry.contains(name)) {
                command = BasicCommand.create(
                    CommandRegistry.opcodeForName(name),
                    name
                );

                tokenIterator.advance();
                tokenIterator.assertNextAndAdvance(OpenParen, "Missing bracket after command name.");

                List<Token> argumentTokens = tokenIterator.readCurrentLevel(OpenParen, CloseParen);
                parseArgumentList(argumentTokens);
            }
        }
    }

    private Optional<Argument> parseArgumentTokens(List<Token> argTokens, int index) {
        if(argTokens.size() != 1) {
            Util.emitFatalError("Invalid argument");
        }

        ConcreteType realType;

        try {
            realType = CommandRegistry.get(command.highLevel).concreteParamTypes().get(index);
        } catch(Exception ignored) {
            return Optional.empty();
        }

        Token token = argTokens.get(0);

        Argument argument = null;
        switch(token.type) {
            case FloatLiteral:
                argument = new Argument(ConcreteType.F32, token.getFloat());
                break;

            case IntLiteral:
                argument = new Argument(realType, token.getInteger());
                break;

            case IdentifierOrKeyword:
                if(!token.getText().matches("&?(global|local)[^_]+_\\d+")) {
                    Util.emitFatalError("Variable names are not yet supported");
                }

                String offsetString = token.getText().split("_")[1];
                argument = new Argument(realType, Integer.parseInt(offsetString));
                break;

            case StringLiteral:
                argument = new Argument(realType, token.getText());
                break;
        }

        return Optional.ofNullable(argument);
    }

    private void parseArgumentList(List<Token> tokens) {
        var argumentGroups = ParserUtils.splitCurrentLevel(tokens, OpenParen, CloseParen, Comma);

        for(int i = 0; i < argumentGroups.size(); i++) {
            List<Token> group = argumentGroups.get(i);

            parseArgumentTokens(group, i).ifPresentOrElse(command.arguments::add, () ->
                Util.emitFatalError("Invalid argument")
            );
        }
    }
}
