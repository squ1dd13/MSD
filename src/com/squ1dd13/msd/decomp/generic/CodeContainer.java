package com.squ1dd13.msd.decomp.generic;

import com.squ1dd13.msd.decomp.*;

import java.util.*;
import java.util.stream.*;

public abstract class CodeContainer<T> extends CodeElement {
    public List<T> bodyCode = new ArrayList<T>();

    public CodeContainer() {}

    public CodeContainer(List<T> body) {
        bodyCode = body;
    }

    public List<BasicScript.Instruction> getBodyInstructions() {
        return bodyCode.stream().map(codeItem -> {
            if(!(codeItem instanceof BasicScript.Instruction)) {
                System.err.println("code item is not a basic instruction, expect an exception");
            }

            return (BasicScript.Instruction)codeItem;
        }).collect(Collectors.toList());
    }

    @Override
    public Set<Integer> getInstructionOffsets() {
        Set<Integer> offsets = new HashSet<>();
        for(T element : bodyCode) {
            if(element instanceof CodeElement) {
                offsets.addAll(((CodeElement)element).getInstructionOffsets());
            }
        }

        return offsets;
    }
}
