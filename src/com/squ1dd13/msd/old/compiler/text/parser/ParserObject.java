package com.squ1dd13.msd.old.compiler.text.parser;

import com.squ1dd13.msd.old.compiler.*;
import com.squ1dd13.msd.old.compiler.language.*;
import com.squ1dd13.msd.old.compiler.text.lexer.*;
import com.squ1dd13.msd.old.shared.*;

import java.util.*;

import static com.squ1dd13.msd.old.compiler.text.lexer.Token.TokenType.*;

public class ParserObject {
    private final List<Compilable> parsedCodeStructures = new ArrayList<>();
    private final List<ParsedClass> parsedClasses = new ArrayList<>();
    private final TokenIterator tokenIterator;

    public ParserObject(List<Token> tokens) {
        tokenIterator = new TokenIterator(ParserUtils.filterBlankTokens(tokens));
    }

    private static List<Argument> argumentsFromTokens(List<Token> tokens) {
        List<Argument> arguments = new ArrayList<>();

        var argumentGroups = ParserUtils.splitCurrentLevel(tokens, OpenParen, CloseParen, Comma);
        for(List<Token> group : argumentGroups) {

        }

        return new ArrayList<>();
    }

    public Optional<BasicCommand> parseNextCommand() {
        // If the next token isn't a command name, this isn't a command (and if it is a command, it's invalid).
        var next = tokenIterator.peek();

        if(next.is(IdentifierOrKeyword)) {
            String name = next.getText();

            if(CommandRegistry.contains(name)) {
                tokenIterator.advance();
                tokenIterator.assertNextAndAdvance(OpenParen, "Missing bracket after command name.");

                List<Token> argumentTokens = tokenIterator.readCurrentLevel(OpenParen, CloseParen);

            }
        }

        return Optional.empty();
    }

    public List<ParsedClass> getClasses() {
        return parsedClasses;
    }

    public List<Compilable> getCodeStructures() {
        return parsedCodeStructures;
    }
}
