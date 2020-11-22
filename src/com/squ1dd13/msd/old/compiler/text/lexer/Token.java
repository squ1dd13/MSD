package com.squ1dd13.msd.old.compiler.text.lexer;

import java.util.*;

public class Token {
    public boolean is(TokenType type) {
        return this.type == type;
    }

    public boolean isNot(TokenType typ) {
        return !is(typ);
    }

    public enum TokenType {
        Whitespace,
        Newline,
        OpenParen,
        CloseParen,
        OpenSquare,
        CloseSquare,
        Comma,
        Equals,
        Operator,
        Semicolon,
        Colon,
        OpenBrace,
        CloseBrace,
        IdentifierOrKeyword,
        IntLiteral,
        FloatLiteral,
        StringLiteral,
        None
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

        final String[] defaults = {
            " ",
            "\\n",
            "(",
            ")",
            "[",
            "]",
            ",",
            "=",
            "?",
            ";",
            ":",
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

    public List<Token> children = new ArrayList<>();

    @Override
    public String toString() {
        String valueString = customText == null
            ? customFloat == 0 ? Integer.toString(customInt) : Float.toString(customFloat)
            : customText;

        return String.format(
            "Token(%s, %s)",
            type.name(),
            valueString
        );
    }
}
