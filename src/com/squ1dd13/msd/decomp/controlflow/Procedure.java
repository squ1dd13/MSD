package com.squ1dd13.msd.decomp.controlflow;

import com.squ1dd13.msd.decomp.*;
import com.squ1dd13.msd.decomp.generic.*;
import com.squ1dd13.msd.shared.*;

public class Procedure extends CodeContainer<CodeElement> {
    public int offset;

    public Procedure(int off) {
        offset = off;
    }

    @Override
    public String toCodeString(int indent) {
        // Indentation starts at 0 for procedures.
        StringBuilder procedureBuilder = new StringBuilder("void proc_").append(offset).append("() {\n");

        for(int i = 0; i < bodyCode.size(); i++) {
            CodeElement element = bodyCode.get(i);

            if(i == bodyCode.size() - 1) {
                if(element instanceof BasicScript.Instruction) {
                    if(((BasicScript.Instruction)element).opcode == Opcode.Return.get()) {
                        continue;
                    }
                }
            }

            procedureBuilder.append(element.toCodeString(4)).append('\n');
        }

        return procedureBuilder.append('}').toString();
    }
}
