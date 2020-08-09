package com.squ1dd13.msd.compiler.text;

import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.util.*;

public class Lexer {
    public static class Token {
        public boolean is(TokenType type) {
            return this.type == type;
        }

        public boolean isNot(TokenType typ) {
            return !is(typ);
        }

        public enum TokenType {
            Whitespace,
            Newline,
            OpenBracket,
            CloseBracket,
            Comma,
            OpenBrace,
            CloseBrace,
            IdentifierOrKeyword,
            IntLiteral,
            FloatLiteral,
            StringLiteral
        }

        public TokenType type;

        public static Token withType(TokenType type) {
            Token t = new Token();
            t.type = type;

            return t;
        }

        private int ordinal() {
            return type.ordinal();
        }

        public boolean hasText = false;
        private String customText = null;
        public String getText() {
            if(customText != null) return customText;

            final String[] defaults = new String[] {
                " ",
                "\n",
                "(",
                ")",
                ",",
                "{",
                "}",
                "?",
                "?",
                "?"
            };

            return ordinal() < defaults.length ? defaults[ordinal()] : "?";
        }

        public boolean hasFloat = false;
        private float customFloat = 0;
        public float getFloat() {
            return customFloat;
        }

        public boolean hasInt = false;
        private int customInt = 0;
        public int getInteger() {
            return customInt;
        }

        public Token withText(String s) {
            Token t = this;
            t.customText = s;
            t.hasText = true;

            return t;
        }

        public Token withInt(int i) {
            Token t = this;
            t.customInt = i;
            t.hasInt = true;

            return t;
        }

        public Token withFloat(float f) {
            Token t = this;
            t.customFloat = f;
            t.hasFloat = true;

            return t;
        }

        @Override
        public String toString() {
            String valueString = customText == null
                ? getFloat() == 0 ? Integer.toString(getInteger()) : Float.toString(getFloat())
                : "'" + customText + "'";

            return String.format(
                "Token(%s, %s)",
                type.name(),
                valueString
            );
        }
    }

    private static Token readStringLiteral(CharacterReader reader) throws IOException {
        boolean escapeNext = false;

        StringBuilder builder = new StringBuilder();

        int c;
        while((c = reader.read()) != -1) {
            if(c == '\'' && !escapeNext) {
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
            || c == '?';
    }

    private static Token readNumber(char first, CharacterReader reader) throws IOException {
        final String numberLiteralChars = ".xf1234567890";
        StringBuilder builder = new StringBuilder().append(first);

        int c;
        while((c = reader.peek()) != -1 && numberLiteralChars.contains(String.valueOf((char)c))) {
            builder.append((char)reader.read());
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

        // A preprocessor should be used to change all newlines to '\n'.
        switch(next) {
            case '\n':
                return Token.withType(Token.TokenType.Newline);

            case '(':
                return Token.withType(Token.TokenType.OpenBracket);

            case ')':
                return Token.withType(Token.TokenType.CloseBracket);

            case ',':
                return Token.withType(Token.TokenType.Comma);

            case '{':
                return Token.withType(Token.TokenType.OpenBrace);

            case '}':
                return Token.withType(Token.TokenType.CloseBrace);

            case '\'': {
                // String literal
                return readStringLiteral(reader);
            }
        }

        if(Character.isWhitespace(next)) {
            return Token.withType(Token.TokenType.Whitespace);
        }

        if(Character.isDigit(next) || ".-".contains(String.valueOf(next))) {
            return readNumber(next, reader);
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

    public static List<Token> lex(String str) throws IOException {
        List<Token> tokens = new ArrayList<>();

        CharacterReader reader = new CharacterReader(new StringReader(str));

        Token t;
        while((t = readNext(reader)) != null) {
            tokens.add(t);
        }

        return tokens;
    }
}
