package com.squ1dd13.msd.compiler.text.ast;

import com.squ1dd13.msd.compiler.text.*;
import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.compiler.text.lexer.Token.*;
import com.squ1dd13.msd.compiler.text.ast.nodes.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

public class NodeFactory {
    public static ASTNode createValueNode(Token token) {
        ValueNode node = new ValueNode();

        if(token.is(TokenType.IdentifierOrKeyword)) {
            if(token.getText().equals("null")) {
                node.isNull = true;
                return node;
            }

            node.isIdentifier = true;
            node.stringValue = token.getText();

            return node;
        }

        if(token.hasFloat) {
            node.floatValue = token.getFloat();
            return node;
        }

        if(token.hasInt) {
            node.intValue = token.getInteger();
            return node;
        }

        if(token.hasText) {
            node.stringValue = token.getText();
            return node;
        }

        return node;
    }

    public static ASTNode createNode(List<Token> tokens) {
        final NewParser.TokenPattern callPattern = new NewParser.TokenPattern(
            Token.withType(TokenType.IdentifierOrKeyword),
            Token.withType(TokenType.OpenBracket)
        );

        List<Token> filtered = NewParser.filterBlankTokens(tokens);

        if(!callPattern.matchesInList(filtered).isEmpty()) {
            // Command or function call (probably).
            CallNode node = new CallNode();
            node.targetName = filtered.get(0).getText();


//            var splitArguments =
        }

        if(tokens.size() > 1) {
            Util.emitWarning("Creating value node when multiple tokens provided.");
        }

        return createValueNode(filtered.get(0));
    }
}
