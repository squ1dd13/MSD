package com.squ1dd13.msd.decompiler;

import com.squ1dd13.msd.shared.*;

import java.util.*;

// TODO: Remove DecompiledScript
public class DecompiledScript {
    public List<Command> commands = new ArrayList<>();

    public void print() {
        for(Command command : commands) {
            System.out.println(command.offset + ": " + command.formattedString());
        }
    }
}
