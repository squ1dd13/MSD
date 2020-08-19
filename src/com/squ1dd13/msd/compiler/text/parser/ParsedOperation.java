package com.squ1dd13.msd.compiler.text.parser;

import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.shared.*;

import java.security.*;
import java.util.*;

public class ParsedOperation {
    private AbstractType declType;
    private String declClass;

    private Token left, right;

    public boolean isDeclaration() {
        return declType != null || declClass != null;
    }

    public ParsedOperation(Iterator<Token> tokenIterator) {
        left = tokenIterator.next();

        AbstractType leftType = null;
        try {
            declType = AbstractType.valueOf(left.getText());
            left = tokenIterator.next();
        } catch(InvalidParameterException ignored) {
            if(ClassRegistry.getClass(left.getText()).isPresent()) {
                declClass = left.getText();
                left = tokenIterator.next();
            }
        }

        Token operator = tokenIterator.next();
        if(isDeclaration() && !operator.getText().equals("=")) {
            Util.emitFatalError("Type is only given when declaring a variable (with '=').");
        }

        right = tokenIterator.next();
    }
}
