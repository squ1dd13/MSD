package com.squ1dd13.msd.decomp.controlflow;

import com.squ1dd13.msd.decomp.*;
import com.squ1dd13.msd.decomp.generic.*;

import java.util.*;

/*
while(x) { ... }
and
do { ... } while(x);

The C-style syntax above gives a very good view of how these statements can be found when compiled.

while(x) is:
    if condition:
        do thing
        jump back to if

do ... while(x) is:
    do thing
    if condition:
        jump back to "do thing"

 */

public class WhileLoop extends Conditional {
    public boolean isDoWhile;
//    private final List<BasicScript.Instruction> conditions = new ArrayList<>();

    public WhileLoop(BasicScript.Instruction ifInstruction) {
        super(ifInstruction);

        conditions = new ArrayList<>();
    }

    public WhileLoop(Conditional other) {
        super(other);

        // If this object is being converted from a normal conditional, then it isn't a do...while loop.
        this.isDoWhile = false;
    }

    public void addCondition(BasicScript.Instruction instruction) {
        conditions.add(instruction);
    }

    @Override
    public String toCodeString(int indent) {
        String indentStr = " ".repeat(indent);

        if(isDoWhile) {
            StringBuilder builder = new StringBuilder(indentStr).append("do {\n");
            for(CodeElement element : bodyCode) {
                builder.append(element.toCodeString(indent + 4)).append('\n');
            }

            return builder.append(indentStr).append("} while(").append(conditionsString()).append(");").toString();
        }

        // If this is a normal while loop, the output is the same as a conditional, except "if" becomes "while".
        String parentStr = super.toCodeString(indent);
        return parentStr.replaceFirst("if", "while");
    }
}
