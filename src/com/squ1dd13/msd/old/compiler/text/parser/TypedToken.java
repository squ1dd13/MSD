package com.squ1dd13.msd.old.compiler.text.parser;

import com.squ1dd13.msd.old.compiler.text.lexer.*;

public class TypedToken {
    public Token token;
    public ObjectType type;

    public TypedToken(Token token, ObjectType type) {
        this.token = token;
        this.type = type;
    }

    public static TypedToken wrap(Token token) {
        return new TypedToken(token, null);
    }
}
