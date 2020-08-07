package com.squ1dd13.msd.decompiler.high.blocks;

import com.squ1dd13.msd.shared.*;

import java.util.*;

public class BlockFactory {
    public static Set<Integer> calledAddresses = new HashSet<>();
    private static boolean insideLabel = false;
    private static boolean insideSub = false;

    public static class FactoryOutput {
        public CodeBlock block;
        public int consumed;

        public FactoryOutput(CodeBlock block, int consumed) {
            this.block = block;
            this.consumed = consumed;
        }
    }

    public static FactoryOutput createBlock(List<Command> commands, int index) {
        Command command = commands.get(index);

        if(command.opcode == 0x50 && calledAddresses.contains(command.jumpDest())) {
            return new FactoryOutput(new CallBlock(command), 1);
        }

        if(!insideSub) {
            if(calledAddresses.contains(command.offset)) {
                insideSub = true;
                SubroutineBlock subBlock = new SubroutineBlock(commands, index);
                insideSub = false;

                return new FactoryOutput(subBlock, subBlock.consumed);
            }
        }

        if(!insideLabel) {
            // Check for jumps to this offset.
            for(Command cmd : commands) {
                if(cmd.isJump() && Math.abs(cmd.arguments[0].intValue) == command.offset) {
                    // If we set insideLabel to true, the next createBlock call will not look for labels.
                    insideLabel = true;

                    FactoryOutput output = createBlock(commands, index);
                    LabelBlock labelBlock = new LabelBlock(command.offset, output.block);

                    insideLabel = false;

                    return new FactoryOutput(labelBlock, output.consumed);
                }
            }
        }

        // Check for 'if' opcode.
        if(command.opcode == 0xD6) {
            ConditionalBlock conditionalBlock = new ConditionalBlock(commands, index);

            if(conditionalBlock.consumed != 0) {
                return new FactoryOutput(conditionalBlock, conditionalBlock.consumed);
            }
        }

        return new FactoryOutput(new SingleCommand(command), 1);
    }
}
