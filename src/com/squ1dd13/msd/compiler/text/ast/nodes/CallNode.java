package com.squ1dd13.msd.compiler.text.ast.nodes;

import com.squ1dd13.msd.compiler.text.ast.*;

import java.util.*;

// Command/subroutine calls
public class CallNode implements ASTNode {
    public String targetName;
    public List<ASTNode> argumentNodes;

    @Override
    public List<ASTNode> getChildren() {
        return argumentNodes;
    }
}
