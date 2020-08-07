package com.squ1dd13.msd.decompiler.high.blocks;

import com.squ1dd13.msd.shared.*;

import java.util.*;

public class GenericCodeBlock implements CodeBlock {
    public List<CodeBlock> commands = new ArrayList<>();

    public GenericCodeBlock() {};

    public GenericCodeBlock(Command command) {
        commands.add(new SingleCommand(command));
    }

    public void add(Command command) {
        commands.add(new SingleCommand(command));
    }

    public void add(CodeBlock block) {
        commands.add(block);
    }

    @Override
    public List<String> toLineStrings() {
        List<String> lineStrings = new ArrayList<>();

        for(CodeBlock block : commands) {
            var lines = block.toLineStrings();

            lineStrings.addAll(lines);
        }

        return lineStrings;
    }
}
