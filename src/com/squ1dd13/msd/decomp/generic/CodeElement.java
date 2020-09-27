package com.squ1dd13.msd.decomp.generic;

import java.util.*;

// Any individually identifiable component that can be executed.
// E.g. a single instruction, an if statement, a procedure, etc.
public abstract class CodeElement {
    public abstract String toCodeString(int indent);
    public abstract Set<Integer> getInstructionOffsets();

    public boolean isLabel;
}
