package com.squ1dd13.msd.old.decompiler.high.blocks;

import com.squ1dd13.msd.old.shared.*;

import java.util.*;

// TODO: Check if this is the first reference to a local variable.
public class DeclarationBlock implements CodeBlock {
    Command command;

    public DeclarationBlock(Command command) {
        this.command = command;
    }

    @Override
    public List<String> toLineStrings() {
        return null;
    }
}
