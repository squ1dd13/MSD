package com.squ1dd13.msd.high.blocks;

import com.squ1dd13.msd.uni.*;

import java.util.*;

// The smallest code block: a single command.
// All other code blocks are made up of these.
public class SingleCommand implements CodeBlock {
    Command command;

    public SingleCommand(Command command) {
        this.command = command;
    }

    @Override
    public List<String> toLineStrings() {
        return List.of(command.formattedString());
    }
}
