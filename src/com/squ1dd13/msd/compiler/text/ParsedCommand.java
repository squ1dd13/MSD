package com.squ1dd13.msd.compiler.text;

import com.squ1dd13.msd.compiler.constructs.*;

import java.util.*;

public class ParsedCommand implements CanBeCompilable {
    public Lexer.Token nameToken;
    public List<Lexer.Token> argumentTokens;

    @Override
    public Compilable toCompilable() {
        return null;
    }
}
