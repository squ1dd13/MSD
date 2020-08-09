package com.squ1dd13.msd.compiler.text.ast.nodes;

import com.squ1dd13.msd.compiler.text.ast.*;

import java.util.*;

public class ValueNode implements ASTNode {
    public String stringValue;
    public int intValue;
    public float floatValue;

    public boolean isIdentifier;
    public boolean isNull;

    @Override
    public List<ASTNode> getChildren() {
        return new ArrayList<>();
    }
}
