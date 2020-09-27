package com.squ1dd13.msd.decomp.controlflow;

/*

switch_start(variable, case count, has default case, 'default' case location, case value, case location, case value, case location...)

The default case location points to the end of the switch if 'has default case' is false.
 */

import com.squ1dd13.msd.decomp.*;
import com.squ1dd13.msd.decomp.generic.*;

import java.util.*;

public class Switch extends CodeElement {
    public BasicScript.BasicValue switchValue;
    private final Map<Integer, Case> caseMap = new HashMap<>();
    private Case defaultCase;

    public void addCase(int value, Case theCase) {
        caseMap.put(value, theCase);
    }

    @Override
    public String toCodeString(int indent) {
        String indentStr = " ".repeat(indent);

        StringBuilder builder = new StringBuilder(indentStr).append("switch(").append(switchValue).append(") {\n");
        for(Case aCase : caseMap.values()) {
            builder.append(aCase.toCodeString(indent + 4)).append('\n');
        }

        if(defaultCase != null) {
            builder.append(defaultCase.toCodeString(indent));
        }

        return builder.append(indentStr).append("}").toString();
    }

    @Override
    public Set<Integer> getInstructionOffsets() {
        return new HashSet<>();
    }

    public static class Case extends CodeContainer<CodeElement> {
        public int value;
        public boolean isDefault;

        @Override
        public String toCodeString(int indent) {
            String indentStr = " ".repeat(indent);

            StringBuilder builder = new StringBuilder(indentStr);
            if(isDefault) {
                builder.append("default: {\n");
            } else {
                builder.append("case ").append(value).append(": {\n");
            }

            for(CodeElement element : bodyCode) {
                builder.append(element.toCodeString(indent + 4)).append('\n');
            }

            return builder.append(indentStr).append("}").toString();
        }
    }

    public static class Info {
        public int caseCount;
        public BasicScript.BasicValue switchValue;
        public int isDefault;

        public Map<Integer, Integer> caseValues = new HashMap<>();
        public List<Integer> caseOffsets = new ArrayList<>();
        public int defaultCaseOffset;

        public Info(BasicScript.Instruction instruction) {
            switchValue = instruction.getArg(0);
            caseCount = instruction.getArg(1).getInt();
            isDefault = instruction.getArg(2).getInt();

            defaultCaseOffset = Math.abs(instruction.getArg(3).getInt());

            for(int i = 0; i < caseCount; ++i) {
                int caseValue = instruction.getArg(4 + i * 2).getInt();
                int caseOffset = Math.abs(instruction.getArg(5 + i * 2).getInt());

                caseOffsets.add(caseOffset);
                caseValues.put(caseOffset, caseValue);
            }
        }
    }
}
