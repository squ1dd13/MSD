package com.squ1dd13.msd.compiler.text.parser;

import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.util.*;

// using abc = xyz;
// global meaningfulName = 12345;
// A 'using' statement (also 'global') allows meaningful names
//  to be given to variables or variable locations.
public class UsingStatement implements IdentifierChanger {
    // Using/global statements are very simple: if we're given an
    //  identifier that is the same as 'input', we change it to 'output'.
    private final String input;
    private final Token output;

    public UsingStatement(List<Token> statementTokens) {
        final Set<String> forbiddenKeywords = Set.of(
            "if",
            "while",
            "for",
            "and",
            "or"
        );

        Token keywordToken = statementTokens.get(0);
        String statementType = keywordToken.getText();

        boolean isGlobalStatement = statementType.equals("global");
        final String syntaxErrorString =
            String.format(
                "'%s' statements must be in the format: '%s abc = xyz'",
                statementType,
                statementType
            );

        Token inputIdentifierToken = statementTokens.get(1);
        if(forbiddenKeywords.contains(inputIdentifierToken.getText())) {
            Util.emitFatalError(String.format("'%s' statements may not change the meaning of keywords", statementType));
        }

        Token equalsToken = statementTokens.get(2);
        if(equalsToken.isNot(Token.TokenType.Equals)) {
            Util.emitFatalError(syntaxErrorString);
        }

        input = inputIdentifierToken.getText();

        if(isGlobalStatement) {
            // 'global' statements use offsets ("global blah = 123"),
            //  so we need to turn the offset into a valid identifier.
            output = Token
                .withType(Token.TokenType.IdentifierOrKeyword)
                .withText("globalThing_" + statementTokens.get(3).getInteger());
        } else {
            output = statementTokens.get(3);
        }
    }

    @Override
    public Token modifyIdentifier(Token identifier) {
        return identifier.getText().equals(input) ? output : identifier;
    }
}
