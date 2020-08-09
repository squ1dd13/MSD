package com.squ1dd13.msd.compiler.text;

import com.squ1dd13.msd.compiler.constructs.*;
import com.squ1dd13.msd.compiler.constructs.language.*;
import com.squ1dd13.msd.compiler.text.lexer.*;

import java.util.*;

public class ParsedConditional implements CanBeCompilable {
    public Token ifToken;
    public List<Token> conditionTokens = null;
    public List<Token> bodyTokens = null;
    public int tokenLength;

    public ParsedConditional(List<Token> unfiltered) {
        List<Token> tokens = NewParser.filterBlankTokens(unfiltered);

        ifToken = tokens.get(0);

        if(ifToken.isNot(Token.TokenType.IdentifierOrKeyword)
            || !ifToken.getText().equals("if")) {
            return;
        }

        int closingBracketPos = -1;

        for(int currentLevel = 0, i = 1; i < tokens.size(); ++i) {
            var tkn = tokens.get(i);

            if(tkn.is(Token.TokenType.OpenBracket)) {
                currentLevel++;
            } else if(tkn.is(Token.TokenType.CloseBracket)) {
                currentLevel--;
            }

            if(currentLevel == 0) {
                closingBracketPos = i;
                break;
            }
        }

        if(closingBracketPos < 0) return;

        conditionTokens = tokens.subList(2, closingBracketPos);

        bodyTokens = new ArrayList<>();

        for(int currentLevel = 1, i = closingBracketPos + 2; i < tokens.size(); ++i) {
            var tkn = tokens.get(i);

            if(tkn.is(Token.TokenType.OpenBrace)) {
                currentLevel++;
            } else if(tkn.is(Token.TokenType.CloseBrace)) {
                currentLevel--;
            }

            if(currentLevel == 0) {
                break;
            }

            bodyTokens.add(tkn);
        }

        // if( = 2 tokens
        // ){ = 2 tokens (whitespace is removed)
        // } = 1 token

        tokenLength = conditionTokens.size() + bodyTokens.size() + 5;
    }

    @Override
    public Compilable toCompilable() {
        Conditional conditional = new Conditional();

        // TODO: Allow mixing and/or in if statements.

        for(var t : conditionTokens) {
            if(t.is(Token.TokenType.IdentifierOrKeyword) || t.getText().equals("and")) {
                conditional.isAnd = true;
                break;
            }
        }

        var conditionTokenLists = NewParser.splitTokens(
            conditionTokens,
            Token
                .withType(Token.TokenType.IdentifierOrKeyword)
                .withText(conditional.isAnd ? "and" : "or")
        );

        conditional.conditions = new ArrayList<>();
        for(var tokenList : conditionTokenLists) {
            tokenList.add(Token.withType(Token.TokenType.Semicolon));

            conditional.conditions.add(
                (LowLevelCommand)new NewParser(tokenList).readCommand().toCompilable()
            );
        }

        conditional.mainBodyElements = new NewParser(bodyTokens).parseTokens();

        return conditional;
    }
}
