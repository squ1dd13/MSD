package com.squ1dd13.msd.old.decompiler.high.blocks;

import com.squ1dd13.msd.old.shared.*;

import java.util.*;

public class ArithmeticBlock implements CodeBlock {
    private final List<VariableOperation> operations = new ArrayList<>();

    public ArithmeticBlock(Command command) {
        operations.add(VariableOperation.forCommand(command));
    }

    public ArithmeticBlock(List<Command> commands) {
        commands.forEach(this::add);
    }

    public void add(Command command) {
        operations.add(VariableOperation.forCommand(command));
    }

    @Override
    public List<String> toLineStrings() {
        List<String> strings = new ArrayList<>();
        strings.add("");

        for(int i = 1; i < operations.size();) {
            VariableOperation previous = operations.get(i - 1);
            VariableOperation current = operations.get(i);

            var combinedStrings = previous.combineWith(current);
            if(combinedStrings.size() == 2) {
                ++i;
                strings.add(previous.toString());
            } else if(combinedStrings.size() == 1) {
                i += 2;
                strings.addAll(combinedStrings);
            }
        }

        strings.add("");
        return strings;
    }
}
