package com.squ1dd13.msd.decomp.controlflow;

import com.squ1dd13.msd.decomp.*;
import com.squ1dd13.msd.decomp.generic.*;

import java.util.*;

public class Conditional extends CodeContainer<CodeElement> {
    public List<BasicScript.Instruction> conditions;
    public ElseBlock elseBlock;
    private CombinationType combination = CombinationType.None;
    public final int conditionCount;
    private final int ifOffset;

    // Takes the offset of and the argument to the 'if' instruction.
    public Conditional(BasicScript.Instruction ifInstruction) {
        int offset = ifInstruction.offset;
        int arg = ifInstruction.getArg(0).getInt();

        ifOffset = offset;
        if(arg == 0) {
            // Type 0 = 1 condition
            conditionCount = 1;
        } else if(arg < 8) {
            // Types 1->7 = 2->8 conditions and combination AND.
            combination = CombinationType.And;
            conditionCount = arg + 1;
        } else if(arg < 28) {
            // Types 21->27 = 2->8 conditions and combination OR.
            combination = CombinationType.Or;
            conditionCount = arg - 19;
        } else {
            conditionCount = -1;
        }
    }

    public Conditional(Conditional other) {
        conditions = other.conditions;
        elseBlock = other.elseBlock;
        combination = other.combination;
        conditionCount = other.conditionCount;
        ifOffset = other.ifOffset;
        bodyCode = other.bodyCode;
    }

    // Attempts to fix dumb if-statements that have empty 'if' bodies and huge 'else' bodies.
    // Such statements are an accurate decompilation, but are difficult to read and look weird.
    public void invertIfNeeded() {
        if(!bodyCode.isEmpty() || elseBlock == null) {
            return;
        }

        // TODO: Invert multiple conditions.
        if(conditions.size() != 1) return;

        conditions.get(0).inverted = !conditions.get(0).inverted;

        bodyCode.addAll(elseBlock.bodyCode);
        elseBlock = null;
    }

    protected String conditionsString() {
        if(conditions == null) {
            return "<null condition list>";
        }

        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < conditions.size(); ++i) {
            builder.append(conditions.get(i).toConditionString());

            if(i != conditions.size() - 1) {
                builder.append(' ').append(combination).append(' ');
            }
        }

        return builder.toString();
    }

    @Override
    public String toCodeString(int indent) {
        String prefix = isLabel ? "label_" + ifOffset + ":\n" : "";
        String indentStr = " ".repeat(indent);
        StringBuilder builder = new StringBuilder(indentStr).append("if(").append(conditionsString());
        builder.append(") {\n");

        for(CodeElement element : bodyCode) {
            builder.append(element.toCodeString(indent + 4)).append('\n');
        }

        builder.append(indentStr).append("}");

        if(elseBlock != null) {
            builder.append(elseBlock.toCodeString(indent));
        }

        return prefix + builder.toString();
    }

    public static class ElseBlock extends CodeContainer<CodeElement> {
        @Override
        public String toCodeString(int indent) {
            // We don't use the indent for the first line because this block should only be used
            //  on a line created by Conditional::toCodeString(), which means it'll be indented already.
            StringBuilder builder = new StringBuilder(" else {\n");
            for(CodeElement element : bodyCode) {
                builder.append(element.toCodeString(indent + 4)).append('\n');
            }

            // We do use the indent here, though.
            return builder.append(" ".repeat(indent)).append("}").toString();
        }
    }

    public enum CombinationType {
        None(""),
        And("and"),
        Or("or");

        String word;
        CombinationType(String codeWord) {
            word = codeWord;
        }

        @Override
        public String toString() {
            return word;
        }
    }
}
