package com.squ1dd13.msd.high.blocks;

import com.squ1dd13.msd.uni.*;

import java.util.*;

public class CallBlock implements CodeBlock {
    public Command command;

    public CallBlock(Command command) {
        this.command = command;
    }

    @Override
    public List<String> toLineStrings() {
        return List.of("proc_" + command.jumpDest() + "()");
    }
}
