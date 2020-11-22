package com.squ1dd13.msd.old.decompiler.high.blocks;

import java.util.*;

// Adds a label declaration above the command.
public class LabelBlock implements CodeBlock {
    public int offset;
    public CodeBlock block;

    public LabelBlock(int offset, CodeBlock block) {
        this.offset = offset;
        this.block = block;
    }

    @Override
    public List<String> toLineStrings() {
        List<String> strings = new ArrayList<>(List.of("", "// label_" + offset + ":"));
        strings.addAll(block.toLineStrings());

        return strings;
    }
}
