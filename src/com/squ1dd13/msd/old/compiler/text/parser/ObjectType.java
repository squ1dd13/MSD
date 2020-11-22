package com.squ1dd13.msd.old.compiler.text.parser;

import com.squ1dd13.msd.old.compiler.text.lexer.*;
import com.squ1dd13.msd.old.shared.*;

public class ObjectType {
    private String className;
    private AbstractType type;

    public ObjectType(String className) {
        this.className = className;
    }

    public ObjectType(AbstractType type) {
        this.type = type;
    }

    public ObjectType(Token token) {
        try {
            type = AbstractType.valueOf(token.getText());
        } catch(IllegalArgumentException ignored) {
            if(ClassRegistry.getClass(token.getText()).isPresent()) {
                className = token.getText();
            }
        }
    }

    public String getClassName() {
        return className;
    }

    public AbstractType getType() {
        return type;
    }

    public boolean isNull() {
        return className == null && type == null;
    }
}
