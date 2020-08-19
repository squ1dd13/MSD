package com.squ1dd13.msd.compiler.text.parser;

import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.compiler.text.parser.*;

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
