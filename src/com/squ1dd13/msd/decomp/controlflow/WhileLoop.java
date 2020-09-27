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

public class WhileLoop extends CodeContainer<CodeElement> {
    public boolean isDoWhile;
    private final List<BasicScript.Instruction> conditions = new ArrayList<>();


    @Override
    public String toCodeString(int indent) {
        String indentStr = " ".repeat(indent);

        StringBuilder builder = new StringBuilder(indentStr).append("do {\n");
        for(CodeElement element : bodyCode) {
            builder.append(element.toCodeString(indent + 4)).append('\n');
        }

        return builder.append("} while(...);").toString();
    }
}
