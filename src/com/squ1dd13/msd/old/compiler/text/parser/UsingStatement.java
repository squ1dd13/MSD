package com.squ1dd13.msd.old.compiler.text.parser;

import com.squ1dd13.msd.old.compiler.text.lexer.*;
import com.squ1dd13.msd.old.shared.*;

import java.util.*;

// using abc = xyz;
// global meaningfulName = 12345;
// A 'using' statement (also 'global' or 'local') allows meaningful names
//  to be given to variables or variable locations.
public class UsingStatement implements IdentifierChanger {
    // Using statements are very simple: if we're given an
    //  identifier that is the same as 'input', we change it to 'output'.
    private final String input;
    private final TypedToken output;

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

        boolean isLocalOrGlobalStatement = statementType.equals("global") || statementType.equals("local");
        final String syntaxErrorString =
            String.format(
                "'%s' statements must be in the format: '%s Type abc = xyz'",
                statementType,
                statementType
            );

        Token typeToken = statementTokens.get(1);
        ObjectType type = new ObjectType(typeToken);

        if(type.isNull()) {
            Util.emitFatalError("Unknown type '" + typeToken.getText() + "'");
        }

        Token inputIdentifierToken = statementTokens.get(2);

        if(forbiddenKeywords.contains(inputIdentifierToken.getText())) {
            Util.emitFatalError(String.format("'%s' statements may not change the meaning of keywords", statementType));
        }

        Token equalsToken = statementTokens.get(3);
        if(equalsToken.isNot(Token.TokenType.Equals)) {
            Util.emitFatalError(syntaxErrorString);
        }

        input = inputIdentifierToken.getText();

        if(isLocalOrGlobalStatement) {
            // Local and global statements use offsets ("global blah = 123"),
            //  so we need to turn those offsets into valid identifiers.
            Token outputToken = Token
                .withType(Token.TokenType.IdentifierOrKeyword)
                .withText(statementType + "Thing_" + statementTokens.get(4).getInteger());

            output = new TypedToken(outputToken, type);
        } else {
            output = new TypedToken(statementTokens.get(4), type);
        }
    }

    @Override
    public TypedToken modifyIdentifier(Token identifier) {
        return identifier.getText().equals(input) ? output : TypedToken.wrap(identifier);
    }
}
