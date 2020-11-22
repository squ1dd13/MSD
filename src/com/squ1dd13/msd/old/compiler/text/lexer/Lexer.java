package com.squ1dd13.msd.old.compiler.text.lexer;

import com.squ1dd13.msd.old.compiler.text.*;
import com.squ1dd13.msd.old.shared.*;

import java.io.*;
import java.util.*;

public class Lexer {

    private static Token readStringLiteral(CharacterReader reader) throws IOException {
        boolean escapeNext = false;

        StringBuilder builder = new StringBuilder();

        int c;
        while((c = reader.read()) != -1) {
            if(c == '"' && !escapeNext) {
                break;
            }

            builder.append((char)c);
            escapeNext = c == '\\' && !escapeNext;
        }

        return Token.withType(Token.TokenType.StringLiteral).withText(builder.toString());
    }

    private static boolean isIdentifierLegal(char c) {
        return Character.isAlphabetic(c)
            || Character.isDigit(c)
            || c == '_'
            || c == '?'
            || c == '.'
            || c == '&';
    }

    private static Token readNumber(char first, CharacterReader reader) throws IOException {
        String numberLiteralChars = ".xf1234567890";
        StringBuilder builder = new StringBuilder().append(first);

        int c;
        boolean foundX = false;
        while((c = reader.peek()) != -1 && numberLiteralChars.contains(String.valueOf((char)c))) {
            char character = (char)reader.read();
            if(character == 'x') {
                if(!foundX) {
                    foundX = true;
                    numberLiteralChars += "ABCDEF";
                }
            }

            builder.append(character);
        }

        String stringValue = builder.toString();

        int pointCount = Util.countOccurrences(stringValue, '.');
        int fCount = Util.countOccurrences(stringValue, 'f');
        int xCount = Util.countOccurrences(stringValue, 'x');

        if(pointCount > 1) {
            Util.emitFatalError("Multiple decimal points not allowed in float literal: '" + stringValue + "'");
        } else if(fCount > 1) {
            Util.emitFatalError("Invalid float literal: '" + stringValue + "'");
        } else if(xCount > 1 || (xCount > 0 && !stringValue.startsWith("0x"))) {
            Util.emitFatalError("Invalid integer literal: '" + stringValue + "'");
        }

        if(stringValue.endsWith("f") || pointCount > 0) {
            if(xCount > 0) {
                Util.emitFatalError("Hexadecimal format is only allowed for integer literals: '" + stringValue + "'");
            }

            float floatValue = Float.parseFloat(Util.cropString(stringValue));
            return Token
                .withType(Token.TokenType.FloatLiteral)
                .withFloat(floatValue);
        }

        if(xCount > 0) {
            stringValue = stringValue.substring(stringValue.indexOf('x') + 1);
        }
        int intValue = Integer.parseInt(stringValue, xCount > 0 ? 16 : 10);

        return Token
            .withType(Token.TokenType.IntLiteral)
            .withInt(intValue);
    }

    private static Token readNext(CharacterReader reader) throws IOException {
        int nextInt = reader.read();
        if(nextInt == -1) {
            return null;
        }

        char next = (char)nextInt;
//        if(!Character.isISOControl(next)) return null;

        if(nextInt > 10000) return null;

        // A preprocessor should be used to change all newlines to '\n'.
        switch(next) {
            case '\n':
                return Token.withType(Token.TokenType.Newline);

            case '(':
                return Token.withType(Token.TokenType.OpenParen);

            case ')':
                return Token.withType(Token.TokenType.CloseParen);

            case '[':
                return Token.withType(Token.TokenType.OpenSquare);

            case ']':
                return Token.withType(Token.TokenType.CloseSquare);

            case ',':
                return Token.withType(Token.TokenType.Comma);

            case '=':
                return Token.withType(Token.TokenType.Equals);

            case ';':
                return Token.withType(Token.TokenType.Semicolon);

            case ':':
                return Token.withType(Token.TokenType.Colon);

            case '{':
                return Token.withType(Token.TokenType.OpenBrace);

            case '}':
                return Token.withType(Token.TokenType.CloseBrace);

            case '"': {
                // String literal
                return readStringLiteral(reader);
            }
        }

        if(Character.isWhitespace(next)) {
            return Token.withType(Token.TokenType.Whitespace);
        }

        if(Character.isDigit(next) || ".".contains(String.valueOf(next))) {
            return readNumber(next, reader);
        }

        final Set<String> operators = Set.of("-", "+", "/", "*", "^");
        if(operators.contains(String.valueOf(next))) {
            return Token.withType(Token.TokenType.Operator).withText(String.valueOf(next));
        }

        StringBuilder identifierBuilder = new StringBuilder();

        reader.save();

        int c = nextInt;
        do {
            identifierBuilder.append((char)c);
            if(!isIdentifierLegal((char)reader.peek()) || reader.peek() == -1) break;
        } while((c = reader.read()) != -1);

        if(c == -1) return null;

        return Token.withType(Token.TokenType.IdentifierOrKeyword).withText(identifierBuilder.toString());
    }

    public static List<Token> lex(String str) {
        try {
            String cleanString = Preprocessor.preprocess(str);
            List<Token> tokens = new ArrayList<>();

            CharacterReader reader = new CharacterReader(new StringReader(cleanString));

            Token t;
            while((t = readNext(reader)) != null) {
                tokens.add(t);
            }

            return tokens;
        } catch(Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
